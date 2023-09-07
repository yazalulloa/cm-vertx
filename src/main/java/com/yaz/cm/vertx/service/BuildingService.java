package com.yaz.cm.vertx.service;

import com.yaz.cm.vertx.domain.Paging;
import com.yaz.cm.vertx.persistence.domain.BuildingQuery;
import com.yaz.cm.vertx.persistence.entity.Building;
import com.yaz.cm.vertx.persistence.repository.BuildingRepository;
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
public class BuildingService {

  private final BuildingRepository repository;

  public Single<Long> count() {
    return repository.count();
  }

  public Single<Integer> delete(String id) {
    return repository.delete(id);
  }

  public Single<Paging<Building>> paging(BuildingQuery rateQuery) {
    return Single.zip(count(), list(rateQuery),
        (totalCount, list) -> new Paging<>(totalCount, null, list));
  }

  public Single<Paging<JsonObject>> pagingJson(BuildingQuery rateQuery) {
    return Single.zip(count(), listJson(rateQuery),
        (totalCount, list) -> new Paging<>(totalCount, null, list));
  }

  public Single<List<JsonObject>> listJson(BuildingQuery query) {
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

  public Single<List<Building>> list(BuildingQuery rateQuery) {
    return repository.select(rateQuery)
        .map(rows -> SqlUtil.toList(rows, Building.class));
  }

}
