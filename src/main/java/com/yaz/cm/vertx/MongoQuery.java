package com.yaz.cm.vertx;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MongoQuery {

  public static JsonObject pagination(JsonObject matchQuery, int limit) {
    return new JsonObject()
        .put("$facet", new JsonObject()
            .put("results", new JsonArray()
                .add(matchQuery)
                .add(sort("_id", -1L))
                .add(limit(limit))
            )
            .put("total_count", totalCount())
            .put("query_count", queryCount(matchQuery))
        );
  }

  public static JsonArray queryCount(JsonObject matchQuery) {
    return new JsonArray()
        .add(matchQuery)
        .add(new JsonObject()
            .put("$count", "query_count"));
  }

  public static JsonObject sort(String key, Object value) {
    return new JsonObject()
        .put("$sort", new JsonObject()
            .put(key, value));
  }

  public static JsonArray totalCount() {
    return new JsonArray()
        .add(new JsonObject()
            .put("$group", new JsonObject()
                .put("_id", 0)
                .put("total_count", new JsonObject()
                    .put("$sum", 1L)
                )
            )
        );
  }

  public static JsonObject limit(int limit) {
    return new JsonObject()
        .put("$limit", limit);
  }
}
