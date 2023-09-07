package com.yaz.cm.vertx.service;

import com.yaz.cm.vertx.domain.Currency;
import com.yaz.cm.vertx.persistence.domain.RateQuery;
import com.yaz.cm.vertx.persistence.domain.SortOrder;
import di.TestComponent;
import io.vertx.core.json.Json;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
@Slf4j
class RateServiceTest {

  private static TestComponent component;

  @BeforeAll
  static void setComponent() {
    component = TestComponent.provides();
  }

  @BeforeEach
  void deploy_verticle(VertxTestContext testContext) {
    component.verticleDeployer().deploy().onComplete(testContext.succeedingThenComplete());
  }

  @Test
  void last() throws Throwable {

    final var rateService = component.rateService();

    final var testObserver = rateService.last(Currency.USD, Currency.VED)
        .map(Json::encodePrettily)
        .doOnSuccess(System.out::println)
        .ignoreElement()
        .andThen(rateService.exists(234353325L))
        .doOnSuccess(System.out::println)
        .test();

    testObserver.await(5, TimeUnit.MINUTES);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }

  @Test
  void paging() throws Throwable {

    final var rateService = component.rateService();

    final var rateQuery = RateQuery.builder()
        .lastId(0)
        .limit(30)
        .sortOrder(SortOrder.DESC)
        .build();

    final var testObserver = rateService.list(rateQuery)
        .map(Json::encodePrettily)
        .doOnSuccess(System.out::println)
        .ignoreElement()
        .andThen(rateService.count())
        .test();

    testObserver.await(5, TimeUnit.MINUTES);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }
}