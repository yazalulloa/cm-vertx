import com.fasterxml.jackson.databind.JsonNode;
import com.yaz.cm.vertx.util.JacksonUtil;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
@Slf4j
class VertxServerTest {

  Handler<HttpServerRequest> handler = req -> req.response().end("Hello from Vert.x!");
  HttpServer httpServer;
  int currentPort;
  Vertx vertx;


  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    this.vertx = vertx;

    final var futures = IntStream.iterate(65535, i -> i - 1)
        .limit(10)
        .mapToObj(this::httpServerFuture)
        .toList();

    Future.all(futures)
        .flatMap(f -> httpServerFuture(65535))
        .onComplete(testContext.succeeding(server -> {
          httpServer = server;
          testContext.completeNow();
        }));
  }

  Future<HttpServer> httpServerFuture(int port) {
    return vertx.createHttpServer()
        .requestHandler(handler)
        .listen(port)
        .transform(ar -> {
          if (ar.failed()) {
            log.error("Failed to listen on port {}", port);
            return httpServerFuture(port - 1);
          }

          final var server = ar.result();
          currentPort = server.actualPort();
          log.info("Server listening at {}", server.actualPort());
          return Future.succeededFuture(server);
        });
  }

  @AfterEach
  void closeServer(VertxTestContext testContext) {
    httpServer.close().onComplete(testContext.succeedingThenComplete());
  }

  @Test
  void server() throws IOException {
    final var jsonNode = JacksonUtil.yamlMapper().readTree(new File("config/config.yml"));
    final var node = jsonNode.get("go-test");
    System.out.println(node);
    assert false;
    assert currentPort != 0;
  }

}
