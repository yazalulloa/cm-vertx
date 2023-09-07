package com.yaz.cm.vertx.sse;

import com.yaz.cm.vertx.sse.impl.SSEHandlerImpl;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@VertxGen
public interface SSEHandler extends Handler<RoutingContext> {

  static SSEHandler create() {
    return new SSEHandlerImpl();
  }

  @Fluent
  SSEHandler connectHandler(Handler<SSEConnection> connection);

  @Fluent
  SSEHandler closeHandler(Handler<SSEConnection> connection);
}
