package com.yaz.cm.vertx.persistence.repository;

import com.yaz.cm.vertx.domain.MySqlQueryRequest;
import com.yaz.cm.vertx.persistence.domain.ApartmentQuery;
import com.yaz.cm.vertx.persistence.entity.Apartment;
import com.yaz.cm.vertx.util.StringUtil;
import com.yaz.cm.vertx.verticle.service.MySqlService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.ArrayTuple;
import java.util.Collection;
import java.util.Optional;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApartmentRepository {

  private static final String COLLECTION = "apartments";
  private static final String SELECT = "SELECT * FROM %s".formatted(COLLECTION);
  private static final String DELETE_BY_BUILDING = "DELETE FROM %s WHERE building_id = ?".formatted(COLLECTION);
  private static final String DELETE_BY_ID = "DELETE FROM %s WHERE building_id = ? AND number = ?".formatted(
      COLLECTION);
  private static final String INSERT = "INSERT INTO apartments (building_id, number, name, aliquot, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?);";
  private static final String REPLACE = "REPLACE INTO apartments (building_id, number, name, aliquot, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?);";

  private final MySqlService mySqlService;

  public Single<Long> count() {
    return mySqlService.totalCount(COLLECTION);
  }

    public Single<Integer> delete(String buildingId, String number) {

    return mySqlService.request(MySqlQueryRequest.normal(DELETE_BY_ID, Tuple.of(buildingId, number)))
        .map(SqlResult::rowCount);
  }

  public Single<Integer> deleteByBuildingId(String buildingId) {

    return mySqlService.request(MySqlQueryRequest.normal(DELETE_BY_BUILDING, Tuple.of(buildingId)))
        .map(SqlResult::rowCount);
  }

  public Single<RowSet<Row>> save(Collection<Apartment> apartments) {

    final var tuples = apartments.stream()
        .map(this::tuple)
        .toList();

    final var mySqlBatch = MySqlQueryRequest.batch(INSERT, tuples);

    return mySqlService.request(mySqlBatch);
  }

  public Single<RowSet<Row>> replace(Collection<Apartment> apartments) {

    final var tuples = apartments.stream()
        .map(this::tuple)
        .toList();

    final var mySqlBatch = MySqlQueryRequest.batch(REPLACE, tuples);

    return mySqlService.request(mySqlBatch);
  }

  private Tuple tuple(Apartment apartment) {
    final var params = new ArrayTuple(6);
    params.addValue(apartment.buildingId());
    params.addValue(apartment.number());
    params.addValue(apartment.name());
    params.addValue(apartment.aliquot());
    params.addValue(apartment.createdAt());
    params.addValue(apartment.updatedAt());

    return params;
  }

  public Single<RowSet<Row>> select(ApartmentQuery query) {

    final var buildingId = StringUtil.trimFilter(query.lastBuildingId());
    final var number = StringUtil.trimFilter(query.lastNumber());

    var tupleSize = 1;
    final var stringBuilder = new StringBuilder(SELECT);
    if (buildingId != null && number != null) {

      stringBuilder.append(" WHERE (building_id,number) > (?,?)");
      tupleSize += 2;
    }

    stringBuilder.append(" ORDER BY building_id,number ").append("ASC").append(" LIMIT ?");

    final var params = new ArrayTuple(tupleSize);
    Optional.ofNullable(buildingId).ifPresent(params::addValue);
    Optional.ofNullable(number).ifPresent(params::addValue);
    params.addValue(query.limit());

    log.info("QUERY {}", stringBuilder);
    final var queryRequest = MySqlQueryRequest.normal(stringBuilder.toString(), params);

    return mySqlService.request(queryRequest);
  }
}
