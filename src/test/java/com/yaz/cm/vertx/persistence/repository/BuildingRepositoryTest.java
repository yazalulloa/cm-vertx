package com.yaz.cm.vertx.persistence.repository;

import com.yaz.cm.vertx.persistence.domain.BuildingQuery;
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
class BuildingRepositoryTest {


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
    final var repository = component.buildingRepository();

    final var testObserver = repository.select(BuildingQuery.builder().build())
        .doOnSuccess(SqlUtil::print)
        .test();

    testObserver.await(5, TimeUnit.MINUTES);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }

}