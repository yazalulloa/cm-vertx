package com.yaz.cm.vertx;

import static org.junit.jupiter.api.Assertions.*;

import com.yaz.cm.vertx.persistence.entity.Rate;
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

@Slf4j
@ExtendWith(VertxExtension.class)
class RateMongoServiceTest {

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
  void rates() throws InterruptedException {
    final var rateMongoService = component.rateMongoService();

    final var testObserver = rateMongoService.rates(0)
        .test();

    testObserver.await(5, TimeUnit.MINUTES);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }
}