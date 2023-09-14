package com.yaz.cm.vertx.controller;

import com.yaz.cm.vertx.PagingDataTemplateService;
import com.yaz.cm.vertx.domain.Paging;
import com.yaz.cm.vertx.persistence.domain.ApartmentQuery;
import com.yaz.cm.vertx.service.ApartmentService;
import com.yaz.cm.vertx.util.ConvertUtil;
import com.yaz.cm.vertx.util.StringUtil;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApartmentController implements DataContextProvider {

  private final ApartmentService service;
  private final PagingDataTemplateService templateService;

  public void delete(RoutingContext ctx) {

    final var buildingId = StringUtil.trimFilter(ctx.pathParam("building_id"));
    final var number = StringUtil.trimFilter(ctx.pathParam("number"));

    if (buildingId == null || number == null) {
      ctx.response().setStatusCode(400).end();
      return;
    }

    service.delete(buildingId, number)
        .subscribe(l -> {
          log.info("Apartment delete {} deleted {} {}", buildingId, number, l);
          ctx.reroute(HttpMethod.GET, "/dynamic/apartment-total-count");
        }, ctx::fail);
  }

  public Single<Paging<JsonObject>> paging(ApartmentQuery query) {

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

  public void counters(RoutingContext ctx) {

    final var q = StringUtil.trimFilter(ctx.queryParams().get("q"));
    final var building = StringUtil.trimFilter(ctx.queryParams().get("building"));
    final var query = ApartmentQuery.of(q, building);

    final var queryCountSingle = service.queryCount(query);
    final var totalCountSingle = service.count();

    Single.zip(queryCountSingle, totalCountSingle, (queryCount, totalCount) -> {
      ctx.data().put("query_count", queryCount.orElse(null));
      ctx.data().put("total_count", totalCount);
      return Optional.empty();
    }).subscribe(map -> ctx.next(), ctx::fail);
  }

  public void totalCount(RoutingContext ctx) {
    service.count()
        .subscribe(totalCount -> {
          ctx.data().put("total_count", totalCount);
          ctx.next();
        }, ctx::fail);
  }

  @Override
  public Single<Map<String, Object>> data(RoutingContext ctx) {

    final var queryParamsMap = ctx.queryParams();
    final var lastBuildingId = StringUtil.trimFilter(queryParamsMap.get("last_building_id"));
    final var lastNumber = StringUtil.trimFilter(queryParamsMap.get("last_number"));
    final var q = StringUtil.trimFilter(queryParamsMap.get("q"));
    final var building = StringUtil.trimFilter(queryParamsMap.get("building"));

    final var isNotFirstPage = lastBuildingId != null && lastNumber != null;

    final var query = ApartmentQuery.builder()
        .lastBuildingId(isNotFirstPage ? lastBuildingId : null)
        .lastNumber(isNotFirstPage ? lastNumber : null)
        .q(q)
        .building(building)
        .build();

    final var actualLimit = query.limit() + 1;
    final var build = query.toBuilder()
        .limit(actualLimit)
        .build();

   /* if (!isNotFirstPage && (q != null || building != null)) {
      log.info("header set");
      ctx.response().putHeader("HX-Trigger", "apt-counters-event");
    }*/

    return paging(build)
        .map(paging -> templateService.data(actualLimit, paging,
            map -> {
              map.put("delete_item_url",
                  "/api/apartments/%s/%s".formatted(map.get("building_id"), map.get("number")));

              final var emails = map.get("emails");
              Optional.ofNullable(emails)
                  .map(Object::toString)
                  .map(str -> Arrays.stream(str.split(","))
                      .toList())
                  .ifPresent(list -> map.put("emails", list));
            },
            map -> {
              var queryParams = "?last_building_id=%s&last_number=%s".formatted(map.get("building_id"),
                  map.get("number"));

              if (query.q() != null) {
                queryParams += "&q=%s".formatted(query.q());
              }

              return queryParams;
            }))
        .doOnSuccess(data -> {

          Optional.ofNullable(data.get("next_page_url"))
              .map(str -> "/dynamic/apartment-card" + str)
              .ifPresent(value -> data.put("next_page_url", value));
        });
  }
}
