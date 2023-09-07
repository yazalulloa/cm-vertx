import com.yaz.cm.vertx.MongoQuery;
import com.yaz.cm.vertx.util.JacksonUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.AggregateOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.FlowableHelper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class MongoClientTest {


  @Test
  void test(Vertx vertx, VertxTestContext testContext) throws Throwable {

    final var url = JacksonUtil.yamlMapper().readTree(new File("config/config.yml"))
        .get("mongodb")
        .get("connection_string")
        .textValue();

    final var config = new JsonObject().put("connection_string", url);
    final var mongoClient = MongoClient.create(vertx, config);

    final var aggregateOptions = new AggregateOptions()
        .setAllowDiskUse(true);

    final var matchQuery = new JsonObject()
        .put("$match", new JsonObject()
            .put("_id", new JsonObject()
                .put("$gt", 290L)));

    final var pagination = MongoQuery.pagination(matchQuery, 10);
    final var pipeline = new JsonArray().add(pagination);

    final var readStream = mongoClient.aggregateWithOptions("rates", pipeline, aggregateOptions);

    System.out.println(pipeline.encode());

    final var completable = FlowableHelper.toFlowable(readStream)
        .toList()
        .map(JsonArray::new)
        .map(JsonArray::encodePrettily)
        .doOnSuccess(System.out::println)
        .ignoreElement();

    completable.subscribe(testContext::completeNow, testContext::failNow);

    testContext.awaitCompletion(60, TimeUnit.SECONDS);
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }
}
