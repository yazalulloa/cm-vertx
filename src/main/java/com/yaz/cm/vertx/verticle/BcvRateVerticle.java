package com.yaz.cm.vertx.verticle;

import com.yaz.cm.vertx.verticle.service.bcv.BcvUsdRateParser;
import com.yaz.cm.vertx.domain.Currency;
import com.yaz.cm.vertx.domain.internal.BcvUsdRateResult;
import com.yaz.cm.vertx.service.RateService;
import com.yaz.cm.vertx.service.http.HttpClientRequest;
import com.yaz.cm.vertx.service.http.HttpClientResponse;
import com.yaz.cm.vertx.service.http.HttpLogConfig;
import com.yaz.cm.vertx.service.http.HttpService;
import com.yaz.cm.vertx.vertx.BaseVerticle;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.http.HttpMethod;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class BcvRateVerticle extends BaseVerticle {

  public static final String ADDRESS = "get-bcv-rate";

  private final BcvUsdRateParser bcvUsdRateParser;
  private final HttpService httpService;
  private final RateService rateService;

  @Override
  public void start() throws Exception {
    eventBusConsumerEmptyBody(ADDRESS, this::newRate);
  }

  private Single<HttpClientResponse> send(HttpMethod httpMethod) {
    return httpService.send(httpRequest(httpMethod));
  }

  private HttpClientRequest httpRequest(HttpMethod httpMethod) {
    return HttpClientRequest.builder()
        .httpMethod(httpMethod)
        .url(config().getString("url"))
        .trustAll(true)
        .responseLogConfig(HttpLogConfig.builder()
            .showBody(false)
            .build())
        .build();
  }

  public Single<BcvUsdRateResult> newRate() {
    final var newRateSingle = send(HttpMethod.GET)
        .map(bcvUsdRateParser::parse)
        .map(newRate -> new BcvUsdRateResult(BcvUsdRateResult.State.NEW_RATE, newRate));

    return rateService.last(Currency.USD, Currency.VED)
        .filter(rate -> rate.etag() != null)
        .flatMapSingle(rate -> {

          return send(HttpMethod.HEAD)
              .flatMap(response -> {
                final var etag = response.headers().get("etag");
                final var lastModified = response.headers().get("last-modified");

                if (etag.equals(rate.etag())) {
                  return Single.just(new BcvUsdRateResult(BcvUsdRateResult.State.ETAG_IS_SAME));
                } else {
                  return newRateSingle;
                }

              });

        })
        .switchIfEmpty(newRateSingle);
  }
}
