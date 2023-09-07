package com.yaz.cm.vertx.verticle.service.bcv;

import com.yaz.cm.vertx.domain.internal.BcvUsdRateResult;
import com.yaz.cm.vertx.verticle.BcvRateVerticle;
import com.yaz.cm.vertx.vertx.VertxHandler;
import io.reactivex.rxjava3.core.Single;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class GetBcvUsdRate {

  private final VertxHandler vertxHandler;


  public Single<BcvUsdRateResult> newRate() {
    return vertxHandler.get(BcvRateVerticle.ADDRESS);
  }
}
