package com.yaz.cm.vertx.service;

import com.yaz.cm.vertx.domain.Currency;
import com.yaz.cm.vertx.domain.Paging;
import com.yaz.cm.vertx.persistence.domain.RateQuery;
import com.yaz.cm.vertx.persistence.entity.Rate;
import com.yaz.cm.vertx.persistence.repository.RateRepository;
import com.yaz.cm.vertx.util.SqlUtil;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RateService {

  private final RateRepository rateRepository;

  public Single<Long> count() {
    return rateRepository.count();
  }

  public Single<Integer> delete(long id) {
    return rateRepository.delete(id);
  }

  public Single<Paging<Rate>> paging(RateQuery rateQuery) {
    return Single.zip(count(), list(rateQuery),
        (totalCount, list) -> new Paging<>(totalCount, null, list));
  }

  public Single<Paging<JsonObject>> pagingJson(RateQuery rateQuery) {
    return Single.zip(count(), listJson(rateQuery),
        (totalCount, list) -> new Paging<>(totalCount, null, list));
  }

  public Single<List<JsonObject>> listJson(RateQuery rateQuery) {
    return rateRepository.listRows(rateQuery)
        .map(rows -> {
          final var list = new ArrayList<JsonObject>();
          for (Row row : rows) {
            final var json = row.toJson();
            list.add(json);
          }
          return list;
        });
  }

  public Single<List<Rate>> list(RateQuery rateQuery) {
    return rateRepository.listRows(rateQuery)
        .map(rows -> SqlUtil.toList(rows, Rate.class));
  }


  public Maybe<Rate> last(Currency fromCurrency, Currency toCurrency) {

    return rateRepository.last(fromCurrency, toCurrency)
        .flatMapMaybe(rows -> {
          final var iterator = rows.iterator();

          if (!iterator.hasNext()) {
            return Maybe.empty();
          }

          final var row = iterator.next();
          final var json = row.toJson().encode();
          final var rate = Json.decodeValue(json, Rate.class);
          return Maybe.just(rate);
        });
  }

  public Single<Rate> save(Rate rate) {

    return rateRepository.save(rate)
        .map(id -> {
          return rate.toBuilder()
              .id(id.orElse(null))
              .build();
        });
  }

  public Single<Boolean> exists(Long hash) {
    return rateRepository.exists(hash)
        .map(RowSet::iterator)
        .map(RowIterator::hasNext);
  }
}
