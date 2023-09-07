package com.yaz.cm.vertx.verticle;

import com.yaz.cm.vertx.vertx.BaseVerticle;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.AggregateOptions;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.rxjava3.FlowableHelper;

public class MongoVerticle extends BaseVerticle {

  public static final String RATES_REPORT = address("rates-report");

  public static final String AGGREGATION = address("aggregation");
  private MongoClient client;

  @Override
  public void start() throws Exception {
    super.start();
    client = MongoClient.createShared(vertx, config());
    eventBusFuture(RATES_REPORT, this::ratesReport);
    eventBusConsumer(AGGREGATION, this::aggregation);
  }

  private Single<JsonObject> aggregation(JsonObject jsonObject) {
    logger.info("AGGREGATION {}", jsonObject);

    final var aggregateOptions = new AggregateOptions()
        .setAllowDiskUse(true);

    final var collection = jsonObject.getString("collection");
    final var pipeline = jsonObject.getJsonArray("pipeline");

    final var readStream = client.aggregateWithOptions(collection, pipeline, aggregateOptions);

    return FlowableHelper.toFlowable(readStream)
        .firstOrError()
        .map(result -> {
          result.getJsonArray("results")
              .forEach(o -> modify((JsonObject) o));

          return result;
        });
  }

  private void modify(JsonObject json) {
    json.remove("_class");
    final var id = json.remove("_id");
    if (id != null) {
      json.put("id", id);
    }
    json.forEach(entry -> {

      if (entry.getValue() instanceof JsonObject) {
        final var date = ((JsonObject) entry.getValue()).getString("$date");
        json.put(entry.getKey(), date);
      }

    });
  }

  private Future<JsonObject> ratesReport(JsonObject jsonObject) {

    final var skip = jsonObject.getInteger("skip", 0);
    final var limit = jsonObject.getInteger("limit", 10);

    final var findOptions = new FindOptions()
        .setSort(new JsonObject().put("_id", -1))
        //.setSkip(skip)
        .setLimit(limit);

    final var query = new JsonObject()
        .put("_id", new JsonObject().put("$gte", 0));

    return client.findWithOptions("rates", query, findOptions)
        .map(list -> {
          return list.stream()
              .peek(this::modify)
              .toList();
        })
        .map(JsonArray::new)
        .map(array -> new JsonObject().put("results", array));
  }
}
