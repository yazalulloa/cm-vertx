package com.yaz.cm.vertx.util;

import io.reactivex.rxjava3.core.Single;

public interface PagingProcessor<T> {

    Single<T> next();

    boolean isComplete();

    void onTerminate();
}
