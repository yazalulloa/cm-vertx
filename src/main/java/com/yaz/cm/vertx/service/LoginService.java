package com.yaz.cm.vertx.service;

import com.yaz.cm.vertx.domain.internal.Result;
import com.yaz.cm.vertx.domain.internal.error.ResponseError;
import com.yaz.cm.vertx.persistence.domain.GoogleUserData;
import com.yaz.cm.vertx.service.http.HttpService;
import com.yaz.cm.vertx.verticle.GoogleVerticle;
import com.yaz.cm.vertx.vertx.VertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class LoginService {

  private final VertxHandler vertxHandler;
  private final HttpService httpService;
  private final OAuth2Auth authProvider;
  private final TemplateEngine templateEngine;


  public void login(RoutingContext ctx) {

    final var session = ctx.session();
    final var user = ctx.user();

    log.info("ctx_session {} {} {}", session.toString(), session.id(), session.data());
    final var principal = user.principal();
    final var idToken = principal.getString("id_token");
    final var attributes = user.attributes();

    log.info("USER {} \nPRINCIPAL {} \nattr {}", user, principal.encodePrettily(), attributes.encodePrettily());

    final var userInfoSingle = vertxHandler.single(authProvider.userInfo(ctx.user()));

    final var tokenSingle = vertxHandler.<Result<JsonObject, ResponseError>>get(GoogleVerticle.VERIFY_TOKEN, idToken);

    Single.zip(userInfoSingle, tokenSingle, (userInfo, result) -> {

          if (result.failed()) {
            return result;
          }

          final var verifyToken = result.obj();
          log.info("VERIFY_TOKEN {}", verifyToken.encodePrettily());
          log.info("USER_INFO {}", userInfo.encodePrettily());

          final var userData = GoogleUserData.builder()
              .sub(userInfo.getString("id"))
              .givenName(userInfo.getString("given_name"))
              .name(userInfo.getString("name"))
              .email(userInfo.getString("email"))
              .emailVerified(userInfo.getBoolean("verified_email"))
              .picture(userInfo.getString("picture"))
              .locale(userInfo.getString("locale"))
              .iat(attributes.getLong("iat"))
              .exp(attributes.getLong("exp"))
              .atHash(verifyToken.getString("at_hash"))
              .accessToken(principal.getString("access_token"))
              .expiresIn(principal.getLong("expires_in"))
              .scope(principal.getString("scope"))
              .tokenType(principal.getString("token_type"))
              .idToken(principal.getString("id_token"))
              .build();

          return result.withObj(userData);
        })
        .subscribe(result -> {

          if (result.error() != null) {
            ctx.response().setStatusCode(result.error().httpCode())
                .end(result.error().responseMsg());
          } else {
            ctx.redirect("/");
          }
        }, ctx::fail);


  }

  public void loginPage(RoutingContext ctx) {
    ctx.addEndHandler(r -> {
      templateEngine.clearCache();
    });
    vertxHandler.vertx().eventBus().<JsonObject>request(GoogleVerticle.GET_SIGN_IN_CONFIG, null)
        .map(Message::body)
        .map(JsonObject::getMap)
        .flatMap(map -> templateEngine.render(map, "login"))
        .onSuccess(ctx::end)
        .onFailure(ctx::fail);
  }
}
