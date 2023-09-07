package com.yaz.cm.vertx.vertx;

import io.micrometer.core.instrument.Metrics;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.micrometer.backends.BackendRegistries;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class BaseVerticle extends Rx3Verticle {

  public static String address(String addressIdentifier) {
    Objects.requireNonNull(addressIdentifier, "addressIdentifier MUST NOT BE NULL");
    final var trim = addressIdentifier.trim();
    if (trim.isEmpty()) {
      throw new RuntimeException("ADDRESS_MUST_NO_BE_EMPTY");
    }

    return addressIdentifier + "_" + UUID.randomUUID();
  }

  protected <T> void eventBusFunction(String address, Function<T, ?> function) {
    vertx.eventBus().<T>consumer(address, message -> {
      try {
        message.reply(function.apply(message.body()));
      } catch (Throwable e) {
        handle(e, message);
      }
    });
  }


  protected <T> void eventBusConsumer(String address, Function<T, Single<?>> function) {
    final var meterRegistry = BackendRegistries.getDefaultNow();
    final var timer = meterRegistry.timer(address);
    vertx.eventBus().<T>consumer(address, message -> {
      long start = System.nanoTime();
      Single.defer(() -> function.apply(message.body()))
          .doAfterTerminate(() -> timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS))
          .subscribe(singleObserver(message));
    });
  }

  protected <T> void eventBusFuture(String address, Function<T, Future<?>> function) {
    vertx.eventBus().<T>consumer(address, message -> eventFuture(message, function));
  }

  protected <T> void eventFuture(Message<T> message, Function<T, Future<?>> function) {
    try {
      function.apply(message.body())
          .onComplete(ar -> {
            if (ar.failed()) {
              handle(message, ar.cause());
            } else {

              message.reply(ar.result());
            }

          });
    } catch (Throwable e) {
      handle(e, message);
    }
  }


  protected <T> void eventBusConsumerEmptyBody(String address, Supplier<Single<?>> supplier) {
    vertx.eventBus().<T>consumer(address, message -> Single.defer(supplier::get).subscribe(singleObserver(message)));
  }

  protected <T> void eventBusSupplier(String address, Supplier<?> supplier) {
    vertx.eventBus().<T>consumer(address, message -> {
      try {
        message.reply(supplier.get());
      } catch (Exception e) {
        handle(e, message);
      }
    });
  }

  protected <T> void eventBusFutureSupplier(String address, Supplier<Future<?>> supplier) {
    vertx.eventBus().<T>consumer(address, message -> {
      try {
        supplier.get()
            .onComplete(ar -> {
              if (ar.failed()) {
                handle(ar.cause(), message);
              } else {
                message.reply(ar.result());
              }

            });
      } catch (Throwable e) {
        handle(e, message);
      }
    });
  }

}
