package com.yaz.cm.vertx;

import com.yaz.cm.vertx.persistence.entity.Rate;
import com.yaz.cm.vertx.util.RxUtil;
import com.yaz.cm.vertx.verticle.MongoVerticle;
import com.yaz.cm.vertx.vertx.VertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MongoService {

  private final VertxHandler vertxHandler;

  public Single<JsonObject> apartmentsAggregation() {
    final var jsonObject = new JsonObject()
        .put("collection", "apartments")
        .put("pipeline", new JsonArray()
            .add(new JsonObject()
                .put("$group", new JsonObject()
                    .put("_id", "$city")
                    .put("count", new JsonObject()
                        .put("$sum", 1))))
            .add(new JsonObject()
                .put("$sort", new JsonObject()
                    .put("count", -1))));

    return vertxHandler.get(MongoVerticle.AGGREGATION, jsonObject);
  }

  public Single<JsonObject> rateAggregation(long lastId) {
    final var idQuery = new JsonObject()
        //.put("$gt", 290L)
        ;

    if (lastId > 0) {
      idQuery.put("$lt", lastId);
    } else {
      idQuery.put("$gt", 0);
    }

    final var matchQuery = new JsonObject()
        .put("$match", new JsonObject()
            .put("_id", idQuery));

    final var pagination = MongoQuery.pagination(matchQuery, 30);
    final var pipeline = new JsonArray().add(pagination);

    final var jsonObject = new JsonObject()
        .put("collection", "rates")
        .put("pipeline", pipeline);

    return vertxHandler.get(MongoVerticle.AGGREGATION, jsonObject);
  }


  public Single<List<Rate>> rates() {

    return vertxHandler.<JsonObject>get(MongoVerticle.RATES_REPORT)
        .map(jsonObject -> RxUtil.fromJsonResults(jsonObject, Rate.class));
  }
}
