package com.yaz.cm.vertx.verticle.service;

import com.yaz.cm.vertx.domain.MySqlQueryRequest;
import com.yaz.cm.vertx.verticle.MySqlVerticle;
import com.yaz.cm.vertx.vertx.VertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.util.List;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MySqlService {

  private static final String TOTAL_COUNT = "SELECT count(*) as total_count FROM %s";

  private final VertxHandler vertxHandler;

  public Single<Long> totalCount(String collection) {

    final var sql = TOTAL_COUNT.formatted(collection);
    return extractLong(MySqlQueryRequest.normal(sql), "total_count");
  }

  public Single<Long> extractLong(MySqlQueryRequest queryRequest, String field) {
    return request(queryRequest)
        .map(rows -> {
          final var rowIterator = rows.iterator();
          if (rowIterator.hasNext()) {
            final var row = rowIterator.next();
            return row.getLong(field);
          }
          return 0L;
        });
  }


  public Single<RowSet<Row>> request(String sql) {
    return request(MySqlQueryRequest.normal(sql));
  }

  public Single<RowSet<Row>> request(MySqlQueryRequest request) {
    return vertxHandler.get(MySqlVerticle.REQUEST, request);
  }

  public Single<List<RowSet<Row>>> transaction(List<MySqlQueryRequest> requests) {
    return vertxHandler.get(MySqlVerticle.TRANSACTION, requests);
  }
}
