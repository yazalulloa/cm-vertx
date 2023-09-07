import com.yaz.cm.vertx.persistence.domain.RateQuery;
import com.yaz.cm.vertx.persistence.domain.SortOrder;
import di.TestComponent;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class RateTemplateTest {

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
  void test(VertxTestContext testContext) throws Throwable {

    final var templateService = component.templateService();
    final var templateEngine = component.templateEngine();
    final var vertxHandler = component.vertxHandler();
    final var rateService = component.rateService();

    final var rateQuery = RateQuery.builder()
        .lastId(0)
        .sortOrder(SortOrder.DESC)
        .limit(30)
        .build();

    final var actualLimit = rateQuery.limit() + 1;
    final var build = rateQuery.toBuilder()
        .limit(actualLimit)
        .build();

   /* rateService.pagingJson(build)
        .map(paging -> templateService.data(actualLimit, paging,
            Comparator.comparing((JsonObject o) -> o.getLong("id")).reversed(),
            map -> "?last_id=" + map.get("id").toString()))
        .map(data -> templateEngine.render(data, "rates"))
        .flatMap(vertxHandler::single)
        .doOnSuccess(System.out::println)
        .subscribe(b -> testContext.completeNow(), testContext::failNow);

    testContext.awaitCompletion(60, TimeUnit.SECONDS);
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }*/
  }


}
