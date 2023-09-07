package com.yaz.cm.vertx;

import com.yaz.cm.vertx.util.RxUtil;
import di.TestComponent;
import io.reactivex.rxjava3.core.Completable;
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
class RatePagingProcessorImplTest {

  private static TestComponent component;

  @BeforeAll
  static void setComponent() {
    component = TestComponent.provides();
  }

  @BeforeEach
  public void deploy_verticle(VertxTestContext testContext) {
    component.verticleDeployer().deploy().onComplete(testContext.succeedingThenComplete());
  }


  @Test
  void test() throws InterruptedException {

    final var pagingProcessor = component.ratePagingProcessorImpl();

    final var testObserver = RxUtil.paging(pagingProcessor, list -> {

      log.info("{}", list.size());
      return Completable.complete();
    }).test();

    testObserver.await(5, TimeUnit.MINUTES);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }
}