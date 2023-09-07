package com.yaz.cm.vertx.vertx;


import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;

@VertxGen
public interface RequestLogHandler extends LoggerHandler {

    static RequestLogHandler create(Vertx vertx) {
        return create(LoggerFormat.DEFAULT, vertx);
    }

    static RequestLogHandler create(LoggerFormat format, Vertx vertx) {
        return new RequestLogger(format, vertx);
    }
}
