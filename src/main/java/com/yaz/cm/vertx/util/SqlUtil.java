package com.yaz.cm.vertx.util;

import io.vertx.core.json.Json;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlUtil {

  private SqlUtil() {
  }

  public static <T> List<T> toList(RowSet<Row> rows, Class<T> clazz) {
    final var list = new ArrayList<T>(rows.size());

    for (Row row : rows) {
      final var json = row.toJson().encode();
      final var obj = Json.decodeValue(json, clazz);
      list.add(obj);
    }

    return list;
  }

  public static void print(RowSet<Row> rowRowSet) {
    log.info("number of the affected rows {} number of rows retrieved {}", rowRowSet.rowCount(),
        rowRowSet.size());
    for (Row row : rowRowSet) {
      log.info("ROW {}", row.toJson());
    }
  }
}
