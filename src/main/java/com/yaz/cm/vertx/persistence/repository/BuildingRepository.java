package com.yaz.cm.vertx.persistence.repository;

import com.yaz.cm.vertx.domain.MySqlQueryRequest;
import com.yaz.cm.vertx.persistence.domain.BuildingQuery;
import com.yaz.cm.vertx.persistence.domain.SortOrder;
import com.yaz.cm.vertx.persistence.entity.Building;
import com.yaz.cm.vertx.verticle.service.MySqlService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.ArrayTuple;
import java.util.Collection;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class BuildingRepository {

  private static final String COLLECTION = "buildings";
  private static final String SELECT = "SELECT * FROM %s".formatted(COLLECTION);
  private static final String DELETE_BY_ID = "DELETE FROM %s WHERE id = ?".formatted(COLLECTION);
  private static final String INSERT = """
      INSERT IGNORE INTO buildings (id, name, rif, main_currency, debt_currency, currencies_to_show_amount_to_pay, fixed_pay, fixed_pay_amount, round_up_payments, amount_of_apts, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
      """;

  private static final String SELECT_ALL_IDS = "SELECT id FROM %s".formatted(COLLECTION);

  private final MySqlService mySqlService;

  public Single<Long> count() {
    return mySqlService.totalCount(COLLECTION);
  }

  public Single<Integer> delete(String id) {

    return mySqlService.request(MySqlQueryRequest.normal(DELETE_BY_ID, Tuple.of(id)))
        .map(SqlResult::rowCount);
  }

  public Single<RowSet<Row>> select(BuildingQuery query) {
    final var stringBuilder = new StringBuilder(SELECT);

    final var lastId = query.lastId();

    final var isLastIdFilter = lastId != null && !lastId.isEmpty();
    if (isLastIdFilter) {
      final var direction = query.sortOrder() == SortOrder.DESC ? "<" : ">";
      stringBuilder.append(" WHERE id ").append(direction).append(" ?");
    }

    stringBuilder.append(" ORDER BY id ").append(query.sortOrder().name()).append(" LIMIT ?");

    var filterSize = 2;
    if (isLastIdFilter) {
      filterSize++;
    }

    final var params = new ArrayTuple(filterSize);
    if (isLastIdFilter) {
      params.addValue(lastId);
    }

    params.addValue(query.limit());
    
    final var queryRequest = MySqlQueryRequest.normal(stringBuilder.toString(), params);

    return mySqlService.request(queryRequest);
  }

  private Tuple tuple(Building building) {
    final var params = new ArrayTuple(10);
    params.addValue(building.id());
    params.addValue(building.name());
    params.addValue(building.rif());
    params.addValue(building.mainCurrency().name());
    params.addValue(building.debtCurrency().name());
    params.addValue(building.currenciesToShowAmountToPay());
    params.addValue(building.fixedPay());
    params.addValue(building.fixedPayAmount());
    params.addValue(building.roundUpPayments());
    params.addValue(building.amountOfApts());
    params.addValue(building.createdAt());
    params.addValue(building.updatedAt());

    return params;
  }

  public Single<RowSet<Row>> save(Collection<Building> buildings) {

    final var tuples = buildings.stream()
        .map(this::tuple)
        .toList();

    final var mySqlBatch = MySqlQueryRequest.batch(INSERT, tuples);

    return mySqlService.request(mySqlBatch);
  }

  public Single<RowSet<Row>> selectAllIds() {
    return mySqlService.request(MySqlQueryRequest.normal(SELECT_ALL_IDS));
  }
}
