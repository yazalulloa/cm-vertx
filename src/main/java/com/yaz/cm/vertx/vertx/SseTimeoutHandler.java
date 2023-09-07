package com.yaz.cm.vertx.vertx;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.PlatformHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SseTimeoutHandler implements PlatformHandler {

  private final TimeoutHandler timeoutHandler;

  @Override
  public void handle(RoutingContext ctx) {
    final var path = ctx.request().path();
    if (path.startsWith("/sse")) {
      ctx.next();
    } else {
      timeoutHandler.handle(ctx);
    }
  }
}
