package com.yaz.cm.vertx.controller;

import com.yaz.cm.vertx.PagingDataTemplateService;
import com.yaz.cm.vertx.domain.Paging;
import com.yaz.cm.vertx.persistence.domain.BuildingQuery;
import com.yaz.cm.vertx.service.BuildingService;
import com.yaz.cm.vertx.util.ConvertUtil;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class BuildingController implements DataContextProvider {

  private final BuildingService service;
  private final PagingDataTemplateService templateService;


  public void delete(RoutingContext ctx) {

    Optional.ofNullable(ctx.pathParam("id"))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .ifPresent(id -> {
          service.delete(id)
              .subscribe(l -> {
                log.info("Building delete {} deleted {}", id, l);
              }, throwable -> {
                log.error("Error deleting {}", id, throwable);
              });
        });

    ctx.response()
        .end();
  }

  public Single<Paging<JsonObject>> paging(BuildingQuery query) {

    return service.pagingJson(query)
        .map(paging -> {
          paging.results()
              .forEach(jsonObject -> {
                ConvertUtil.formatDate("created_at", jsonObject);
                ConvertUtil.formatDate("updated_at", jsonObject);
              });

          return paging;
        });
  }

  public void selector(RoutingContext ctx) {
    service.ids()
        .subscribe(list -> {
          ctx.data().put("buildings", list);
          ctx.next();

        }, ctx::fail);
  }

  @Override
  public Single<Map<String, Object>> data(RoutingContext ctx) {
    final var lastId = Optional.ofNullable(ctx.queryParams().get("last_id"))
        .map(String::trim)
        .orElse("");

    final var buildingQuery = BuildingQuery.builder()
        .lastId(lastId)
        .build();

    final var actualLimit = buildingQuery.limit() + 1;
    final var build = buildingQuery.toBuilder()
        .limit(actualLimit)
        .build();

    return paging(build)
        .map(paging -> templateService.data(actualLimit, paging,
            map -> map.put("delete_item_url", "/api/buildings/" + map.get("id")),
            map -> "?last_id=" + map.get("id").toString()))
        .doOnSuccess(data -> {

          Optional.ofNullable(data.get("next_page_url"))
              .map(str -> "/dynamic/building-card" + str)
              .ifPresent(value -> data.put("next_page_url", value));
        });
  }
}
