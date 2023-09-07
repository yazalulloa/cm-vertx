package com.yaz.cm.vertx.controller;

import com.yaz.cm.vertx.domain.internal.Result;
import com.yaz.cm.vertx.domain.internal.error.ResponseError;
import com.yaz.cm.vertx.verticle.GoogleVerticle;
import com.yaz.cm.vertx.vertx.VertxHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class LoginController {

  private final VertxHandler vertxHandler;

  public void googleRedirect(RoutingContext ctx) {
    final var credential = ctx.request().getParam("credential");
    final var gCsrfToken = ctx.request().getParam("g_csrf_token");

    log.info("credential {} gCsrfToken {}", credential, gCsrfToken);

    vertxHandler.<Result<JsonObject, ResponseError>>get(GoogleVerticle.VERIFY_TOKEN, credential)
        .subscribe(result -> {
          if (result.failed()) {
            ctx.response().setStatusCode(result.error().httpCode())
                .end(result.error().responseMsg());
          } else {
            log.info("USER {}", result.obj().encodePrettily());
            ctx.redirect("/");
          }
        }, ctx::fail);


  }
}
