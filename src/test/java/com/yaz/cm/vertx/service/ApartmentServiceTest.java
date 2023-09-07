package com.yaz.cm.vertx.service;

import com.yaz.cm.vertx.persistence.domain.ApartmentQuery;
import com.yaz.cm.vertx.persistence.entity.Apartment;
import di.TestComponent;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
@Slf4j
class ApartmentServiceTest {

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
  void list() throws InterruptedException {
    final var service = component.apartmentService();

    final var testObserver = service.list(ApartmentQuery.builder().build())
        .doOnSuccess(l -> log.info("LIST {}", l.size()))
        .test();

    testObserver.await(5, TimeUnit.MINUTES);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }
}