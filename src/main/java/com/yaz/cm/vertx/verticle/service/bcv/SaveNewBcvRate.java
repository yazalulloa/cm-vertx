package com.yaz.cm.vertx.verticle.service.bcv;

import com.yaz.cm.vertx.domain.internal.BcvUsdRateResult;
import com.yaz.cm.vertx.domain.internal.BcvUsdRateResult.State;
import com.yaz.cm.vertx.service.NotificationService;
import com.yaz.cm.vertx.service.RateService;
import com.yaz.cm.vertx.util.DateUtil;
import com.yaz.cm.vertx.util.DecimalUtil;
import com.yaz.cm.vertx.vertx.VertxHandler;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Slf4j
public class SaveNewBcvRate {

  private final RateService rateService;
  private final BcvGetDocumentQueue bcvGetDocumentQueue;
  private final NotificationService notificationService;
  private final VertxHandler vertxHandler;

  private Single<BcvUsdRateResult> getNewRate() {
    return vertxHandler.single(bcvGetDocumentQueue::getNewRate);
  }

  public Single<BcvUsdRateResult> saveNewRate() {

    return getNewRate()
        .flatMap(result -> {

          if (result.state() != BcvUsdRateResult.State.NEW_RATE) {
            return Single.just(result);
          }

          final var rate = result.rate();

          final var newRateSingle = Single.fromCallable(() -> rate.toBuilder()
                  .createdAt(DateUtil.utcLocalDateTime())
                  .build())
              .flatMap(rateService::save)
              .map(r -> new BcvUsdRateResult(BcvUsdRateResult.State.NEW_RATE, r))
              .cache();

          final var saveRate = newRateSingle
              .map(BcvUsdRateResult::rate)
              .doAfterSuccess(r -> vertxHandler.publishSse("new_rate"))
              .map(r -> "Nueva tasa añadida%n%s%nFecha de la tasa: %s".formatted(r.rate(), r.dateOfRate()))
              .flatMapCompletable(notificationService::sendNewRate)
              .andThen(newRateSingle);

          final var lastSingle = rateService.last(rate.fromCurrency(), rate.toCurrency())
              .switchIfEmpty(Maybe.fromAction(() -> log.info("LAST RATE NOT FOUND")))
              .map(lastRate -> {

                    if (rate.dateOfRate().isBefore(lastRate.dateOfRate())) {
                      return new BcvUsdRateResult(BcvUsdRateResult.State.OLD_RATE);
                    }

                    final var isSameRate = DecimalUtil.equalsTo(lastRate.rate(), rate.rate())
                        && lastRate.dateOfRate().isEqual(rate.dateOfRate())
                        && lastRate.source() == rate.source();

                    if (!isSameRate) {
                      log.info("LAST RATE IS DIFFERENT \nOLD: {}\nNEW: {}", Json.encodePrettily(lastRate),
                          Json.encodePrettily(rate));
                      return new BcvUsdRateResult(BcvUsdRateResult.State.NEW_RATE);
                    }

                    return new BcvUsdRateResult(BcvUsdRateResult.State.SAME_RATE);
                  }
              )
              .defaultIfEmpty(new BcvUsdRateResult(BcvUsdRateResult.State.RATE_NOT_IN_DB));

          final var existSingle = rateService.exists(rate.hash());

          return Single.zip(lastSingle, existSingle, (lastResult, exists) -> {

            if (lastResult.state() == BcvUsdRateResult.State.SAME_RATE
                || lastResult.state() == BcvUsdRateResult.State.OLD_RATE
                || lastResult.state() == State.ETAG_IS_SAME) {
              return Single.just(lastResult);
            }

            if (exists) {
              return Single.just(new BcvUsdRateResult(BcvUsdRateResult.State.HASH_SAVED));
            }

            return saveRate;
          }).flatMap(s -> s);
        });

  }
}
