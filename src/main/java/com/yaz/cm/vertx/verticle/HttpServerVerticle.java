package com.yaz.cm.vertx.verticle;

import com.yaz.cm.vertx.util.EnvUtil;
import io.netty.handler.codec.compression.StandardCompressionOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class HttpServerVerticle extends AbstractVerticle {

  private final Router router;


  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    final var httpServerOptions = new HttpServerOptions()
        .setPort(EnvUtil.getInt("PORT", 8888))
        .setLogActivity(false)
        .setTcpKeepAlive(true)
        .setSsl(false)
        .setUseAlpn(true)
        .setCompressionSupported(true)
        .addCompressor(StandardCompressionOptions.gzip())
        .addCompressor(StandardCompressionOptions.deflate())
        // .addCompressor(StandardCompressionOptions.brotli())
        .addCompressor(StandardCompressionOptions.zstd());

    vertx.createHttpServer(httpServerOptions)
        .requestHandler(router)
        .listen(http -> {
          if (http.succeeded()) {
            //log.info("HTTP server started on port 8888");
            startPromise.complete();
          } else {
            log.error("Error starting HTTP server", http.cause());
            startPromise.fail(http.cause());
          }
        });
  }
}
