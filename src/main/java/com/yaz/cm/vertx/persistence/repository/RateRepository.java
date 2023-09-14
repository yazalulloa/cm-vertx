package com.yaz.cm.vertx.persistence.repository;

import com.yaz.cm.vertx.domain.Currency;
import com.yaz.cm.vertx.domain.MySqlQueryRequest;
import com.yaz.cm.vertx.persistence.domain.RateQuery;
import com.yaz.cm.vertx.persistence.domain.SortOrder;
import com.yaz.cm.vertx.persistence.entity.Rate;
import com.yaz.cm.vertx.util.SqlUtil;
import com.yaz.cm.vertx.verticle.service.MySqlService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.ArrayTuple;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RateRepository {

  private static final String COLLECTION = "rates";
  private static final String SELECT = "SELECT * FROM %s %s ORDER BY id DESC LIMIT ?";
  private static final String DELETE_BY_ID = "DELETE FROM %s WHERE id = ?".formatted(COLLECTION);
  private static final String INSERT = """
      INSERT INTO rates (from_currency, to_currency, rate, date_of_rate, source, created_at, hash, etag, last_modified) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

  private static final String FULL_INSERT = """
      INSERT IGNORE INTO rates (id, from_currency, to_currency, rate, date_of_rate, source, created_at, hash, etag, last_modified) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

  private final MySqlService mySqlService;

  public Single<Long> count() {
    return mySqlService.totalCount(COLLECTION);
  }

  public Single<Integer> delete(long id) {

    return mySqlService.request(MySqlQueryRequest.normal(DELETE_BY_ID, Tuple.of(id)))
        .map(SqlResult::rowCount);
  }

  public Single<RowSet<Row>> listRows(RateQuery query) {
    final var stringBuilder = new StringBuilder();

    final var tupleSize = new AtomicInteger(1);
    final var shouldSetAnd = new AtomicBoolean(false);

    final var lastIdOptional = Optional.of(query.lastId())
        .filter(l -> l > 0);

    lastIdOptional
        .ifPresent(str -> {
          final var direction = query.sortOrder() == SortOrder.DESC ? "<" : ">";
          stringBuilder.append(" id ").append(direction).append(" ?");
          tupleSize.incrementAndGet();
          shouldSetAnd.set(true);
        });

    Optional.ofNullable(query.date())
        .ifPresent(str -> {

          if (shouldSetAnd.get()) {
            stringBuilder.append(" AND ");
          }

          stringBuilder.append(" date_of_rate <= ?");
          tupleSize.incrementAndGet();
          shouldSetAnd.set(true);
        });

    final var params = new ArrayTuple(tupleSize.get());

    lastIdOptional.ifPresent(params::addValue);
    Optional.ofNullable(query.date()).ifPresent(params::addValue);
    params.addValue(query.limit());

    final var queryRequest = MySqlQueryRequest.normal(SELECT.formatted(COLLECTION, stringBuilder.isEmpty() ? "" : "WHERE " + stringBuilder), params);

    return mySqlService.request(queryRequest);
  }

  public Single<RowSet<Row>> fullSave(List<Rate> rates) {
    final var tuples = rates.stream()
        .map(this::fullTuple)
        .toList();

    final var mySqlBatch = MySqlQueryRequest.batch(FULL_INSERT, tuples);

    return mySqlService.request(mySqlBatch);
  }

  private Tuple fullTuple(Rate rate) {
    final var params = new ArrayTuple(10);
    params.addValue(rate.id());
    rateTuple(rate, params);
    return params;
  }

  private void rateTuple(Rate rate, ArrayTuple params) {
    params.addValue(rate.fromCurrency().name());
    params.addValue(rate.toCurrency().name());
    params.addValue(rate.rate());
    params.addValue(rate.dateOfRate());
    params.addValue(rate.source().name());
    params.addValue(rate.createdAt());
    params.addValue(rate.hash());
    params.addValue(rate.etag());
    params.addValue(rate.lastModified());
  }

  public Single<Optional<Long>> save(Rate rate) {

    final var params = new ArrayTuple(9);
    rateTuple(rate, params);

    final var queryRequest = MySqlQueryRequest.normal(INSERT, params);

    final var list = new ArrayList<MySqlQueryRequest>();
    list.add(queryRequest);
    list.add(MySqlQueryRequest.normal("SELECT LAST_INSERT_ID()"));

    return mySqlService.transaction(list)
        .doOnSuccess(rows -> {
          for (RowSet<Row> rowRowSet : rows) {
            SqlUtil.print(rowRowSet);
          }
        })
        .map(rowSets -> {

          return rowSets.stream()
              .filter(rowSet -> rowSet.size() > 0)
              .findFirst()
              .map(RowSet::iterator)
              .filter(RowIterator::hasNext)
              .map(RowIterator::next)
              .map(row -> row.getLong("LAST_INSERT_ID()"));


        });
  }

  public Single<RowSet<Row>> last(Currency fromCurrency, Currency toCurrency) {
    final var sql = "SELECT * FROM rates WHERE from_currency = ? AND to_currency = ? ORDER BY id DESC LIMIT 1";
    final var queryRequest = MySqlQueryRequest.normal(sql, Tuple.of(fromCurrency.name(), toCurrency.name()));

    return mySqlService.request(queryRequest);
  }

  public Single<RowSet<Row>> exists(Long hash) {
    final var sql = "SELECT * FROM rates WHERE hash = ? LIMIT 1";
    final var queryRequest = MySqlQueryRequest.normal(sql, Tuple.of(hash));

    return mySqlService.request(queryRequest);
  }
}
