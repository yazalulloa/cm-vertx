package com.yaz.cm.vertx.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.sqlclient.Tuple;
import java.awt.Window.Type;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
@EqualsAndHashCode
public class MySqlQueryRequest {

  private final Type type;
  private final String query;
  private final List<Tuple> params;

  public static MySqlQueryRequest batch(String query, List<Tuple> params) {
    return new MySqlQueryRequest(Type.BATCH, query, params);
  }

  public static MySqlQueryRequest normal(String query) {
    return new MySqlQueryRequest(Type.NORMAL, query, Collections.emptyList());
  }

  public static MySqlQueryRequest normal(String query, Tuple tuple) {
    return new MySqlQueryRequest(Type.NORMAL, query, Collections.singletonList(tuple));
  }

  public enum Type {
    BATCH, NORMAL
  }
}
