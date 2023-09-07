package com.yaz.cm.vertx.controller;

import com.yaz.cm.vertx.PagingDataTemplateService;
import com.yaz.cm.vertx.domain.Paging;
import com.yaz.cm.vertx.persistence.domain.RateQuery;
import com.yaz.cm.vertx.service.RateService;
import com.yaz.cm.vertx.util.ConvertUtil;
import com.yaz.cm.vertx.util.DateUtil;
import com.yaz.cm.vertx.verticle.service.bcv.SaveNewBcvRate;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RatesController implements DataContextProvider {

  private final RateService service;
  private final PagingDataTemplateService templateService;
  private final SaveNewBcvRate saveNewBcvRate;

  public void delete(RoutingContext ctx) {
    log.info("REQUEST {}", ctx.request().absoluteURI());

    Optional.ofNullable(ctx.pathParam("id"))
        .map(ConvertUtil::toLong)
        .ifPresent(id -> {
          service.delete(id)
              .subscribe(l -> {
                log.info("Rate delete {} deleted {}", id, l);
              }, throwable -> {
                log.error("Error deleting {}", id, throwable);
              });
        });

    ctx.response()
        .end();
  }

  public void bcvLookUp(RoutingContext ctx) {
    saveNewBcvRate.saveNewRate()
        .subscribe(r -> {
              log.info("RATE BCV RESULT {}", r);
              ctx.response().setStatusCode(204)
                  .end();
            },
            throwable -> {
              log.error("Error BCV", throwable);
              ctx.response().setStatusCode(204)
                  .end();
            });


  }

  public Single<Paging<JsonObject>> paging(RateQuery rateQuery) {

    return service.pagingJson(rateQuery)
        .map(paging -> {
          paging.results()
              .forEach(jsonObject -> {
                final var dateStr = jsonObject.getString("created_at");
                final var formatted = DateUtil.formatVe(ZonedDateTime.of(LocalDateTime.parse(dateStr), ZoneOffset.UTC));
                jsonObject.put("created_at", formatted);
              });

          return paging;
        });
  }

  @Override
  public Single<Map<String, Object>> data(RoutingContext ctx) {
    final var lastId = Optional.ofNullable(ctx.queryParams().get("last_id"))
        .map(Long::parseLong)
        .orElse(0L);

    final var rateQuery = RateQuery.builder()
        .lastId(lastId)
        .build();

    final var actualLimit = rateQuery.limit() + 1;
    final var build = rateQuery.toBuilder()
        .limit(actualLimit)
        .build();

    return paging(build)
        .map(paging -> templateService.data(actualLimit, paging,
            Comparator.comparing((JsonObject o) -> o.getLong("id")).reversed(),
            map -> map.put("delete_item_url", "/api/rates/" + map.get("id")),
            map -> "?last_id=" + map.get("id").toString()))
        .doOnSuccess(data -> {
          Optional.ofNullable(data.get("next_page_url"))
              .map(str -> "/dynamic/rate-card" + str)
              .ifPresent(value -> data.put("next_page_url", value));
        });
  }
}
