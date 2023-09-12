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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApartmentRepository {

  private static final String COLLECTION = "apartments";
  private static final String SELECT = "SELECT * FROM %s".formatted(COLLECTION);
  private static final String QUERY_COUNT = "SELECT count(*) as query_count FROM %s".formatted(COLLECTION);
  private static final String DELETE_BY_BUILDING = "DELETE FROM %s WHERE building_id = ?".formatted(COLLECTION);
  private static final String DELETE_BY_ID = "DELETE FROM %s WHERE building_id = ? AND number = ?".formatted(
      COLLECTION);
  private static final String INSERT = "INSERT INTO apartments (building_id, number, name, aliquot, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?);";
  private static final String REPLACE = "REPLACE INTO apartments (building_id, number, name, aliquot, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?);";

  private static final String INSERT_EMAIL = "INSERT INTO apartment_emails (building_id, apt_number, email) VALUES (?, ?, ?);";
  private static final String REPLACE_EMAIL = "REPLACE INTO apartment_emails (building_id, apt_number, email) VALUES (?, ?, ?);";


  private static final String SELECT_FULL = """
      SELECT apartments.*, GROUP_CONCAT(apartment_emails.email) as emails
      from apartments
               INNER JOIN apartment_emails ON apartments.building_id = apartment_emails.building_id AND
                                              apartments.number = apartment_emails.apt_number
         %s
      GROUP BY apartments.building_id, apartments.number
      ORDER BY apartments.building_id, apartments.number
      LIMIT ?;
      """;

  private static final String SELECT_FULL_WITH_LIKE = """
      SELECT apartments.*, GROUP_CONCAT(apartment_emails.email) as emails
      FROM apartments
               INNER JOIN (SELECT apartments.building_id, apartments.number
                           FROM apartments
                                    LEFT JOIN apartment_emails ON apartments.building_id = apartment_emails.building_id AND
                                                                  apartments.number = apartment_emails.apt_number
                           %s
                           GROUP BY apartments.building_id, apartments.number) AS matched_apartments
                          ON matched_apartments.building_id = apartments.building_id AND
                             matched_apartments.number = apartments.number
               INNER JOIN apartment_emails ON apartments.building_id = apartment_emails.building_id AND
                                              apartments.number = apartment_emails.apt_number
      GROUP BY apartments.building_id, apartments.number
      ORDER BY apartments.building_id, apartments.number
      LIMIT ?;
            """;

  private static final String QUERY_COUNT_WHERE = """
      SELECT COUNT(DISTINCT apartments.building_id, apartments.number) AS query_count
      FROM apartments
      INNER JOIN apartment_emails
      ON apartments.building_id = apartment_emails.building_id
      AND apartments.number = apartment_emails.apt_number
      %s;
      """;

  private static final String CURSOR_QUERY = "(apartments.building_id,apartments.number) > (?,?)";
  private static final String LIKE_QUERY = " concat(apartments.building_id, apartments.number, apartments.name, apartment_emails.email) LIKE ? ";

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

    final var aptBatch = MySqlQueryRequest.batch(REPLACE, tuples);

    final var emailsTuple = apartments.stream().flatMap(this::emailsTuple)
        .toList();

    if (emailsTuple.isEmpty()) {
      return mySqlService.request(aptBatch);
    }

    final var emailBatch = MySqlQueryRequest.batch(REPLACE_EMAIL, emailsTuple);

    return Single.zip(mySqlService.request(aptBatch), mySqlService.request(emailBatch), (apt, email) -> apt);
  }


  public Stream<Tuple> emailsTuple(Apartment apartment) {
    if (apartment.emails() == null || apartment.emails().isEmpty()) {
      return Stream.empty();
    }

    return apartment.emails().stream().map(email -> {
          final var params = new ArrayTuple(3);
          params.addValue(apartment.buildingId());
          params.addValue(apartment.number());
          params.addValue(email);
          return params;
        })
        .map(o -> o);
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

    final var sqlQueryRequest = where(query);

    final var str = sqlQueryRequest.query().isEmpty() ? "" : " WHERE " + sqlQueryRequest.query();
    final var queryRequest = sqlQueryRequest
        .toBuilder()
        .query((query.q() != null ? SELECT_FULL_WITH_LIKE : SELECT_FULL).formatted(str))
        .build();

    return mySqlService.request(queryRequest);
  }

  private MySqlQueryRequest where(ApartmentQuery query) {

    final var buildingId = StringUtil.trimFilter(query.lastBuildingId());
    final var number = StringUtil.trimFilter(query.lastNumber());

    final var stringBuilder = new StringBuilder();
    final var tupleSize = new AtomicInteger(1);
    boolean ifCursorQuery = buildingId != null && number != null;
    if (ifCursorQuery) {
      stringBuilder.append(CURSOR_QUERY);

      tupleSize.addAndGet(2);
    }

    final var buildingOptional = Optional.ofNullable(query.building());
    buildingOptional.ifPresent(str -> {
      if (ifCursorQuery) {
        stringBuilder.append(" AND ");
      }
      stringBuilder.append(" apartments.building_id = ? ");
      tupleSize.addAndGet(1);
    });

    final var qOptional = Optional.ofNullable(query.q())
        .map(String::trim)
        .filter(s -> !s.isEmpty());

    qOptional
        .ifPresent(str -> {
          if (ifCursorQuery) {
            stringBuilder.append(" AND ");
          }
          stringBuilder.append(LIKE_QUERY);
          tupleSize.addAndGet(1);
        });

    final var params = new ArrayTuple(tupleSize.get());
    Optional.ofNullable(buildingId).ifPresent(params::addValue);
    Optional.ofNullable(number).ifPresent(params::addValue);
    buildingOptional.ifPresent(params::addValue);
    qOptional.map(s -> "%" + s + "%").ifPresent(params::addValue);
    params.addValue(query.limit());

    return MySqlQueryRequest.normal(stringBuilder.toString(), params);
  }

  public Single<Optional<Long>> queryCount(ApartmentQuery query) {
    final var q = StringUtil.trimFilter(query.q());
    final var building = StringUtil.trimFilter(query.building());
    if (q != null || building != null) {

      final var stringBuilder = new StringBuilder(" WHERE ");
      boolean setAnd = false;

      final var tupleSize = new AtomicInteger(0);

      if (building != null) {
        stringBuilder.append(" apartments.building_id = ? ");
        setAnd = true;
        tupleSize.incrementAndGet();
      }

      if (q != null) {
        if (setAnd) {
          stringBuilder.append(" AND ");
        }

        stringBuilder.append(LIKE_QUERY);
        tupleSize.incrementAndGet();
      }

      final var tuple = new ArrayTuple(tupleSize.get());
      if (building != null) {
        tuple.addValue(building);
      }

      if (q != null) {
        tuple.addValue("%" + q + "%");
      }

      final var queryRequest = MySqlQueryRequest.normal(QUERY_COUNT_WHERE.formatted(stringBuilder.toString()), tuple);
      return mySqlService.extractLong(queryRequest, "query_count")
          .map(Optional::of);
    }

    return Single.just(Optional.empty());
  }
}
