package com.yaz.cm.vertx.verticle;

import com.yaz.cm.vertx.Application;
import com.yaz.cm.vertx.vertx.VertxProvider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    new Application().run(VertxProvider.configureVertx(vertx), new String[]{});
  }


}

