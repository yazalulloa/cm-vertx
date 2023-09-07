package com.yaz.cm.vertx.service;

import com.yaz.cm.vertx.domain.Paging;
import com.yaz.cm.vertx.persistence.domain.ApartmentQuery;
import com.yaz.cm.vertx.persistence.entity.Apartment;
import com.yaz.cm.vertx.persistence.repository.ApartmentRepository;
import com.yaz.cm.vertx.util.SqlUtil;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import java.util.ArrayList;
import java.util.List;
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

  public Single<Integer> delete(String buildingId, String number) {
    return repository.delete(buildingId, number);
  }

  public Single<Integer> deleteByBuildingId(String buildingId) {
    return repository.deleteByBuildingId(buildingId);
  }

  public Single<Paging<Apartment>> paging(ApartmentQuery rateQuery) {
    return Single.zip(count(), list(rateQuery),
        (totalCount, list) -> new Paging<>(totalCount, null, list));
  }

  public Single<Paging<JsonObject>> pagingJson(ApartmentQuery rateQuery) {
    return Single.zip(count(), listJson(rateQuery),
        (totalCount, list) -> new Paging<>(totalCount, null, list));
  }

  public Single<List<JsonObject>> listJson(ApartmentQuery query) {
    return repository.select(query)
        .map(rows -> {
          final var list = new ArrayList<JsonObject>();
          for (Row row : rows) {
            final var json = row.toJson();
            list.add(json);
          }
          return list;
        });
  }

  public Single<List<Apartment>> list(ApartmentQuery rateQuery) {
    return repository.select(rateQuery)
        .map(rows -> SqlUtil.toList(rows, Apartment.class));
  }
}
