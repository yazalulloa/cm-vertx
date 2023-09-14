package com.yaz.cm.vertx.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.yaz.cm.vertx.persistence.domain.BuildingQuery;
import com.yaz.cm.vertx.persistence.domain.RateQuery;
import com.yaz.cm.vertx.util.SqlUtil;
import di.TestComponent;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(VertxExtension.class)
class RateRepositoryTest {

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
    final var repository = component.rateRepository();

    final var testObserver = repository.listRows(RateQuery.builder()
            .lastId(304)
            .build())
        .doOnSuccess(SqlUtil::print)
        .test();

    testObserver.await(5, TimeUnit.MINUTES);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }



}