package com.yaz.cm.vertx;

import com.yaz.cm.vertx.controller.ApartmentController;
import com.yaz.cm.vertx.controller.AppInfoController;
import com.yaz.cm.vertx.controller.BuildingController;
import com.yaz.cm.vertx.controller.DataController;
import com.yaz.cm.vertx.controller.LoginController;
import com.yaz.cm.vertx.controller.RatesController;
import com.yaz.cm.vertx.domain.constants.GoogleUrls;
import com.yaz.cm.vertx.service.LoginService;
import com.yaz.cm.vertx.sse.SSEHandler;
import com.yaz.cm.vertx.util.Constants;
import com.yaz.cm.vertx.util.EnvUtil;
import com.yaz.cm.vertx.vertx.CacheTemplateHandler;
import com.yaz.cm.vertx.vertx.RequestLogHandler;
import com.yaz.cm.vertx.vertx.SseTimeoutHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.HSTSHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.SecurityPolicyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import io.vertx.ext.web.handler.XFrameHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.cookie.CookieSessionStore;
import io.vertx.micrometer.PrometheusScrapingHandler;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RouterProvider {

  private final Vertx vertx;
  private final TemplateEngine templateEngine;
  private final LoginService loginService;
  private final OAuth2Auth authProvider;
  //private final SimpleAuthenticationProvider simpleAuthenticationProvider;

  private final RatesController ratesController;
  private final BuildingController buildingController;
  private final ApartmentController apartmentController;
  private final AppInfoController appInfoController;
  private final LoginController loginController;

  public Router router() {

    final var healthChecks = HealthChecks.create(vertx);
    healthChecks.register(
        "my-procedure",
        promise -> promise.complete(Status.OK()));

    final var healthCheckHandler = HealthCheckHandler.createWithHealthChecks(healthChecks);

    final var router = Router.router(vertx);

    final var origin = System.getenv("ORIGIN");
    final var googleCallback = System.getenv("GOOGLE_CALLBACK_URL");

    final var googleCallbackRoute = router.route(googleCallback);
    final var authHandler = OAuth2AuthHandler.create(vertx, authProvider,
            origin + googleCallback)
        .pkceVerifierLength(128)
        .withScopes(List.of(
            "openid",
            "email",
            "profile",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
        ))
        .setupCallback(googleCallbackRoute);

    final var csrfHeaderName = CSRFHandler.DEFAULT_HEADER_NAME;

   /* final var allowedHeaders = Stream.of(
            HttpHeaders.ACCEPT,
            HttpHeaders.ACCEPT_LANGUAGE,
            HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
            HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
            HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
            HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
            HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
            //HttpHeaders.ACCESS_CONTROL_MAX_AGE,
            HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CACHE_CONTROL,
            HttpHeaders.CONNECTION,
            HttpHeaders.CONTENT_DISPOSITION,
            HttpHeaders.CONTENT_ENCODING,
            HttpHeaders.CONTENT_LANGUAGE,
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.COOKIE,
            HttpHeaders.ETAG,
            HttpHeaders.EXPIRES,
            HttpHeaders.IF_MATCH,
            HttpHeaders.IF_MODIFIED_SINCE,
            HttpHeaders.IF_NONE_MATCH,
            HttpHeaders.LAST_MODIFIED,
            HttpHeaders.LOCATION,
            HttpHeaders.ORIGIN,
            HttpHeaders.REFERER,
            HttpHeaders.KEEP_ALIVE,
            HttpHeaders.UPGRADE,
            HttpHeaders.VARY,
            "x-requested-with",
            "Last-Event-ID",
            csrfHeaderName
        )
        .map(CharSequence::toString)
        .collect(Collectors.toSet());*/

    final var corsHandler = CorsHandler.create()
        .addOrigin(origin)
        .addOrigin(GoogleUrls.ACCOUNTS)
        .allowedMethods(Set.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.HEAD,
            HttpMethod.OPTIONS, HttpMethod.PATCH))
        .allowCredentials(true)
        .allowedHeader("Accept")
        .allowedHeader("Accept-Language")
        .allowedHeader("Content-Language")
        .allowedHeader("Last-Event-ID")
        .allowedHeader("Content-Type")
        .allowedHeader("Access-Control-Allow-Origin")
        .allowedHeader("Access-Control-Allow-Headers")
        .allowedHeader("Access-Control-Expose-Headers")
        .allowedHeader("Origin")
        .allowedHeader("origin")
        .allowedHeader("Referrer")
        .allowedHeader("Referer")
        .allowedHeader("Authorization")
        .allowedHeader("x-requested-with")
        .allowedHeader(HttpHeaders.VARY.toString())
        .allowedHeader(csrfHeaderName);

    final var csrfHandler = CSRFHandler.create(vertx, System.getenv("CSRF_SECRET"))
        //.setOrigin(origin)
        .setHeaderName(csrfHeaderName);

    final var requestTimeout = EnvUtil.getLong("REQUEST_TIMEOUT");
    final var requestTimeoutUnit = TimeUnit.valueOf(System.getenv("REQUEST_TIMEOUT_UNIT"));

    final var timeoutHandler = TimeoutHandler.create(requestTimeoutUnit.toMillis(requestTimeout));

    final var localSessionStore = LocalSessionStore.create(vertx, "condominium-manager");
    final var cookieSessionStore = CookieSessionStore.create(vertx, System.getenv("COOKIE_SECRET"));

    final var sessionTimeout = EnvUtil.getLong("SESSION_TIMEOUT");
    final var sessionTimeoutUnit = TimeUnit.valueOf(System.getenv("SESSION_TIMEOUT_UNIT"));

    router.route()
        .handler(new SseTimeoutHandler(timeoutHandler))
        .handler(ResponseTimeHandler.create())
        .handler(ResponseContentTypeHandler.create())
        .handler(SessionHandler.create(localSessionStore)
                .setSessionCookieName("uh23sd-dg9321s.sdg912")
                .setSessionTimeout(sessionTimeoutUnit.toMillis(sessionTimeout))
           /* .setCookieless(false)
            .setCookieHttpOnlyFlag(true)
            .setCookieSecureFlag(true)*/
        )
        .handler(EnvUtil.bool("CUSTOM_ACCESS_LOG")
            ? RequestLogHandler.create(vertx)
            : LoggerHandler.create(true, LoggerFormat.DEFAULT))
        .handler(FaviconHandler.create(vertx))
        .handler(corsHandler)
        .handler(HSTSHandler.create())
        //.handler(new CspHandlerProvider().cspHandler())
        .handler(XFrameHandler.create(XFrameHandler.SAMEORIGIN))
        .handler(BodyHandler.create(false));

    SecurityPolicyHandler addHeadersHandler = ctx -> {
      ctx.response().putHeader("Referrer-Policy", "strict-origin-when-cross-origin");
      ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
      ctx.next();
    };

    router.route().failureHandler(ctx -> {
      final var failure = ctx.failure();
      log.info("ROUTER_FAILURE {} {}", ctx.request().method(), ctx.request().path(), failure);
      ctx.redirect("/");
    });

    router.route().handler(addHeadersHandler);

    router.get("/health*").handler(healthCheckHandler);
    router.route("/metrics").handler(PrometheusScrapingHandler.create());

    //router.route(googleCallback).handler(loginController::googleRedirect);

    final var loginRoute = "/login";

    final var redirectAuthHandler = RedirectAuthHandler.create(authProvider, loginRoute);
  /*  router.get(loginRoute)
        .handler(loginService::loginPage);*/
    final var loginHandler = router.route(loginRoute)
        .handler(authHandler);

    googleCallbackRoute.failureHandler(ctx -> {
      log.info("googleCallback {}", googleCallback, ctx.failure());
      ctx.redirect("/");
    });

    loginHandler.handler(loginService::login);

    if (EnvUtil.bool("AUTH_REDIRECT_ENABLE")) {
      router.route("/").handler(redirectAuthHandler);
      router.route("/*").handler(redirectAuthHandler);
    }

    router.route().handler(csrfHandler);

    final var ssePath = "/sse";
    final var sseHandler = SSEHandler.create();
    router.get(ssePath + "/*")
        .produces("text/event-stream")
        .handler(sseHandler);

    sseHandler.connectHandler(connection -> {
      final var path = connection.ctx().request().path();

      if (path.equals(ssePath)) {
        connection.close();
      } else {
        connection.forward(path.replace(ssePath, ""));
      }
    });

    sseHandler.closeHandler(connection -> {
      final var path = connection.ctx().request().path().replace(ssePath, "");

    });

    router.get("/api/rates/bcv-lookup").handler(ratesController::bcvLookUp);
    router.delete("/api/rates/:id").handler(ratesController::delete);
    router.delete("/api/buildings/:id").handler(buildingController::delete);
    router.delete("/api/apartments/:building_id/:number").handler(apartmentController::delete);

    final var cacheTemplateHandler = new CacheTemplateHandler(vertx, templateEngine,
        TemplateHandler.DEFAULT_TEMPLATE_DIRECTORY, TemplateHandler.DEFAULT_CONTENT_TYPE);

    final var templateHandler = cacheTemplateHandler;// TemplateHandler.create(templateEngine);

    final var cacheEnabled = EnvUtil.bool("CACHE_ENABLED");

    Handler<RoutingContext> disableCacheHandler = ctx -> {
      if (!cacheEnabled) {
        ctx.addEndHandler(r -> templateEngine.clearCache());
      }

      templateHandler.handle(ctx);
    };

    router.get("/dynamic/*")
        .handler(cacheTemplateHandler::preHandler);

    final var dataRateController = new DataController(ratesController);
    router.get("/dynamic/rate-card").handler(dataRateController);

    final var dataBuildingController = new DataController(buildingController);
    router.get("/dynamic/buildings").handler(dataBuildingController);
    router.get("/dynamic/building-card").handler(dataBuildingController);
    router.get("/dynamic/buildings-selector").handler(buildingController::selector);

    final var dataApartmentController = new DataController(apartmentController);
    router.get("/dynamic/apartments").handler(dataApartmentController);
    router.get("/dynamic/apartment-card").handler(dataApartmentController);
    router.get("/dynamic/apartment-counters").handler(apartmentController::counters);
    router.get("/dynamic/apartment-total-count").handler(apartmentController::totalCount);

    final var dataAppInfoController = new DataController(appInfoController);
    router.get("/dynamic/app-info").handler(dataAppInfoController);

    router.route("/dynamic/*").handler(disableCacheHandler);

    router.route("/*").handler(ctx -> {

      // log.info("SERVING STATIC {}", ctx.request().path());
      final var path = ctx.request().path();

      if (path.isEmpty() || path.endsWith("/")) {
        ctx.next();
        return;
      }

      final var indexOfDot = path.lastIndexOf(".");
      if (indexOfDot == -1) {
        log.info("REROUTING {} {}", ctx.request().method(), ctx.request().path());
        ctx.reroute(path + ".html");
      } else {
        ctx.next();
      }

    });

    router.route("/*").handler(StaticHandler.create(Constants.STATIC_DIR).setCachingEnabled(cacheEnabled));
    return router;
  }

}
