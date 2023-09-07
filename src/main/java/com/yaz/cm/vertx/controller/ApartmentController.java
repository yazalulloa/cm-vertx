package com.yaz.cm.vertx.controller;

import com.yaz.cm.vertx.PagingDataTemplateService;
import com.yaz.cm.vertx.domain.Paging;
import com.yaz.cm.vertx.persistence.domain.ApartmentQuery;
import com.yaz.cm.vertx.service.ApartmentService;
import com.yaz.cm.vertx.util.ConvertUtil;
import com.yaz.cm.vertx.util.StringUtil;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.data.Numeric;
import java.util.ArrayList;
import java.util.Comparator;
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
        }, throwable -> {
          log.error("Error deleting {} {}", buildingId, number, throwable);
        });

    ctx.response()
        .end();
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

  @Override
  public Single<Map<String, Object>> data(RoutingContext ctx) {

    final var lastBuildingId = StringUtil.trimFilter(ctx.queryParams().get("last_building_id"));
    final var lastNumber = StringUtil.trimFilter(ctx.queryParams().get("last_number"));

    final var b = lastBuildingId != null && lastNumber != null;

    final var query = ApartmentQuery.builder()
        .lastBuildingId(b ? lastBuildingId : null)
        .lastNumber(b ? lastNumber : null)
        .build();

    final var actualLimit = query.limit() + 1;
    final var build = query.toBuilder()
        .limit(actualLimit)
        .build();

    return paging(build)
        .map(paging -> templateService.data(actualLimit, paging,
            Comparator.comparing((JsonObject o) -> o.getString("building_id"))
                .thenComparing((JsonObject o) -> o.getString("number")),
            map -> {
              final var aliquot = (Numeric) map.get("aliquot");
              map.put("aliquot", aliquot.doubleValue());
              map.put("delete_item_url", "/api/apartments/%s/%s".formatted(map.get("building_id"), map.get("number")));
            },
            map -> "?last_building_id=%s&last_number=%s".formatted(map.get("building_id"), map.get("number"))))
        .doOnSuccess(data -> {

          Optional.ofNullable(data.get("next_page_url"))
              .map(str -> "/dynamic/apartment-card" + str)
              .ifPresent(value -> data.put("next_page_url", value));
        });
  }
}
