package com.yaz.cm.vertx;

import com.yaz.cm.vertx.persistence.entity.Rate;
import com.yaz.cm.vertx.util.PagingProcessor;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RatePagingProcessorImpl implements PagingProcessor<List<Rate>> {

  private volatile boolean isComplete;
  private final RateMongoService mongoService;
  private volatile long lastId;

  @Override
  public Single<List<Rate>> next() {
      return list()
          .doOnSuccess(list -> {
            isComplete = list.isEmpty();
            if (!isComplete) {
              lastId = list.get(list.size() - 1).id();
            }
          });
  }

  @Override
  public boolean isComplete() {
    return isComplete;
  }

  @Override
  public void onTerminate() {

  }

  private Single<List<Rate>> list() {
    return mongoService.rates(lastId);
  }
}
