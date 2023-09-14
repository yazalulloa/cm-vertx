package com.yaz.cm.vertx.service;

import com.yaz.cm.vertx.domain.Paging;
import com.yaz.cm.vertx.persistence.domain.ApartmentQuery;
import com.yaz.cm.vertx.persistence.entity.Apartment;
import com.yaz.cm.vertx.persistence.repository.ApartmentRepository;
import com.yaz.cm.vertx.util.SqlUtil;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApartmentService {

  private final ApartmentRepository repository;

  public Single<Long> count() {
    return repository.count();
  }

  public Single<Optional<Long>> queryCount(ApartmentQuery query) {
    return repository.queryCount(query);
  }

  public Single<Integer> delete(String buildingId, String number) {
    return repository.delete(buildingId, number);
  }

  public Single<Integer> deleteByBuildingId(String buildingId) {
    return repository.deleteByBuildingId(buildingId);
  }

  public Single<Paging<Apartment>> paging(ApartmentQuery query) {
    return Single.zip(count(), queryCount(query), list(query),
        (totalCount, queryCount, list) -> new Paging<>(totalCount, queryCount.orElse(null), list));
  }

  public Single<Paging<JsonObject>> pagingJson(ApartmentQuery query) {
    return Single.zip(count(), queryCount(query), listJson(query),
        (totalCount, queryCount, list) -> new Paging<>(totalCount, queryCount.orElse(null), list));
  }

  public Single<List<JsonObject>> listJson(ApartmentQuery query) {
    return repository.select(query)
        .map(SqlUtil::toJsonObject);
  }

  public Single<List<Apartment>> list(ApartmentQuery rateQuery) {
    return repository.select(rateQuery)
        .map(rows -> SqlUtil.toList(rows, Apartment.class));
  }

  public Maybe<Apartment> findOneFull(String buildingId, String number) {
    return repository.findOneFull(buildingId, number)
        .map(RowSet::iterator)
        .filter(RowIterator::hasNext)
        .map(RowIterator::next)
        .map(repository::fromRowFull);
  }
}
