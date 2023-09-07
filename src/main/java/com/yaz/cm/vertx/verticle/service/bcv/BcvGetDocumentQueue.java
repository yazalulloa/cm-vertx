package com.yaz.cm.vertx.verticle.service.bcv;

import com.yaz.cm.vertx.domain.internal.BcvUsdRateResult;
import com.yaz.cm.vertx.util.DateUtil;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class BcvGetDocumentQueue {

  private static final Waiter waiter = new Waiter();

  private final GetBcvUsdRate getBcvUsdRate;

  public void getNewRate(Handler<AsyncResult<BcvUsdRateResult>> resultHandler) {

    if (waiter.isAvailable() && waiter.add(resultHandler)) {
      //log.info("QUEUE_IS_AVAILABLE");
      resolve();
    } else {
     // log.info("QUEUE_IS_NOT_AVAILABLE");
      resultHandler.handle(Future.succeededFuture(new BcvUsdRateResult(BcvUsdRateResult.State.QUEUE_IS_NOT_AVAILABLE)));
    }
  }

  private void resolve() {

    getBcvUsdRate.newRate()
        //.delay(5, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.io())
        .doAfterTerminate(waiter::restart)
        .subscribe(
            rate -> waiter.handle(Future.succeededFuture(rate)),
            error -> waiter.handle(Future.failedFuture(error))
        );

  }

  @SuperBuilder(toBuilder = true)
  @Accessors(fluent = true)
  @ToString
  @Getter
  public static class Waiter {

    private final AtomicBoolean available;
    private final AtomicInteger counter;
    private ZonedDateTime last;
    private Handler<AsyncResult<BcvUsdRateResult>> handler;

    public Waiter() {
      this.available = new AtomicBoolean(true);
      this.counter = new AtomicInteger(0);
    }

    public boolean isAvailable() {
      return available().get();
    }

    public void disable() {
      available().set(false);
    }

    public void restart() {
      available().set(true);
      last = null;
      handler = null;
    }

    public synchronized boolean add(Handler<AsyncResult<BcvUsdRateResult>> resultHandler) {
      if (handler != null) {
        return false;
      }
      available.set(false);
      this.last = DateUtil.nowZonedWithUTC();
      this.handler = resultHandler;
      return true;
    }

    public void handle(AsyncResult<BcvUsdRateResult> result) {
      if (handler != null) {
        handler.handle(result);
      }
    }
  }

}
