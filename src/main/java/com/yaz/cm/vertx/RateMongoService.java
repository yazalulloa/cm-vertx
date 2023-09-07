package com.yaz.cm.vertx;

import com.yaz.cm.vertx.persistence.entity.Rate;
import com.yaz.cm.vertx.util.RxUtil;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RateMongoService {

  private final MongoService mongoService;

  public Single<List<Rate>> rates(long lastId) {

    return mongoService.rateAggregation(lastId)
        .map(jsonObject -> RxUtil.fromJsonResults(jsonObject, Rate.class));
  }
}
