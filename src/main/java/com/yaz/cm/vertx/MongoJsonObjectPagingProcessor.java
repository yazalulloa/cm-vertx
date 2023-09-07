package com.yaz.cm.vertx;

import com.yaz.cm.vertx.util.PagingProcessor;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;

@RequiredArgsConstructor
public class MongoJsonObjectPagingProcessor implements PagingProcessor<List<JsonObject>> {

  private volatile boolean isComplete;
  private final AtomicReference<JsonObject> lastObj = new AtomicReference<>();

  private final ListProvider listFunction;

  @Override
  public Single<List<JsonObject>> next() {
    return listFunction.list(lastObj.get())
        .doOnSuccess(list -> {
          isComplete = list.isEmpty();
          if (!isComplete) {
            lastObj.set(list.get(list.size() - 1));
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

  @FunctionalInterface
  public interface ListProvider {

    Single<List<JsonObject>> list(JsonObject lastObj);
  }
}
