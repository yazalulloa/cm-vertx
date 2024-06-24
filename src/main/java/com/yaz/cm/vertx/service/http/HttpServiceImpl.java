package com.yaz.cm.vertx.service.http;

import com.yaz.cm.vertx.util.rx.RetryWithDelay;
import com.yaz.cm.vertx.verticle.HttpClientVerticle;
import com.yaz.cm.vertx.vertx.VertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.eventbus.DeliveryOptions;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class HttpServiceImpl implements HttpService {

  private static final int MAX_RETRY_COUNT = 5;
  private static final int RETRY_TIME_DELAY = 100;
  private static final TimeUnit RETRY_TIME_UNIT_DELAY = TimeUnit.MILLISECONDS;

  private final VertxHandler handler;

  @Override
  public Single<Long> requestCount() {
    return handler.get(HttpClientVerticle.REQUEST_COUNT);
  }

  @Override
  public Single<HttpClientResponse> send(HttpClientRequest request) {

    final var timeoutTime = Optional.of(request.timeoutTime())
        .filter(o -> o > 1)
        .orElse(3L);

    final var timeUnit = Optional.ofNullable(request.timeoutTimeUnit())
        .orElse(TimeUnit.MINUTES);

    return handler.<HttpClientResponse>get(HttpClientVerticle.SEND, request,
            new DeliveryOptions().setSendTimeout(timeUnit.toMillis(timeoutTime)))
        .retryWhen(RetryWithDelay.retryIfFailedNetwork(MAX_RETRY_COUNT, RETRY_TIME_DELAY, RETRY_TIME_UNIT_DELAY));
  }
}
