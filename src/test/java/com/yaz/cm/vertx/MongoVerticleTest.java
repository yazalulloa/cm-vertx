package com.yaz.cm.vertx;

import com.yaz.cm.vertx.verticle.MongoVerticle;
import di.TestComponent;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
@Slf4j
class MongoVerticleTest {

  private static TestComponent component;

  @BeforeAll
  static void setComponent() {
    log.info("setComponent");
    component = TestComponent.provides();
  }

  @BeforeEach
  void deploy_verticle(VertxTestContext testContext) {
    component.verticleDeployer().deploy().onComplete(testContext.succeedingThenComplete());
  }

  @Test
  void test(VertxTestContext testContext) throws Throwable {
    final var vertx = component.vertx();

    final var matchQuery = new JsonObject()
        .put("$match", new JsonObject()
            .put("_id", new JsonObject()
                .put("$gt", 0)));

    final var pagination = MongoQuery.pagination(matchQuery, 10);
    final var pipeline = new JsonArray().add(pagination);

    final var jsonObject = new JsonObject()
        .put("collection", "rates")
        .put("pipeline", pipeline);

    System.out.println(pipeline.encode());

    vertx.eventBus().request(MongoVerticle.AGGREGATION, jsonObject)
        .map(Message::body)
        .map(d -> {
          System.out.println(Json.encode(d));
          return d;
        })
        .onComplete(testContext.succeedingThenComplete());

    // [{"$facet":{"results":[{"$match":{"_id":{"$gt":290}}},{"$sort":{"_id":-1}},{"$limit":10}],"total_count":[{"$group":{"total":{"$sum":1}}}],"query_count":[{"$match":{"_id":{"$gt":290}}},{"$count":"query_count"}]}}]
    // [{"$facet":{"results":[{"$match":{"_id":{"$gt":290}}},{"$sort":{"_id":-1}},{"$limit":10}],"total_count":[{"$group":{"_id":null,"total":{"$sum":1}}}],"query_count":[{"$match":{"_id":{"$gt":290}}},{"$count":"query_count"}]}}]
  }
}