package com.yaz.cm.vertx.controller;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DataController implements Handler<RoutingContext> {

  private final DataContextProvider dataContextProvider;

  @Override
  public void handle(RoutingContext ctx) {
    dataContextProvider.data(ctx)
        .subscribe(map -> {
              ctx.data().putAll(map);
              ctx.next();
            },
            ctx::fail);
  }
}
