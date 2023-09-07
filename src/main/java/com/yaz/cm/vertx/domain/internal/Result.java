package com.yaz.cm.vertx.domain.internal;

import com.yaz.cm.vertx.domain.internal.error.ResponseError;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class Result<T, E extends ResponseError> {

  private final T obj;
  private final E error;

  private Result(T obj, E error) {
    this.obj = obj;
    this.error = error;
  }

  public T obj() {
    return obj;
  }

  public E error() {
    return error;
  }

  public static <O, Err extends ResponseError> Result<O, Err> success(O obj) {
    return new Result<>(obj, null);
  }

  public static <O, Err extends ResponseError> Result<O, Err> error(Err error) {
    return new Result<>(null, error);
  }

  public <O> Result<O, E> withObj(O obj) {
    return new Result<>(obj, this.error);
  }

  private <O> Result<O, E> transformError() {
    return new Result<>(null, this.error);
  }

  public <R> Single<Result<R, E>> rxSingle(Function<? super T, ? extends Single<Result<R, E>>> function)
      throws Throwable {
    if (this.error != null) {
      return Single.just(this.transformError());
    }

    return function.apply(this.obj);
  }

  public boolean failed() {
    return error != null;
  }
}
