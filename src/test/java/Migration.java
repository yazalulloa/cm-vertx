import com.yaz.cm.vertx.persistence.entity.Apartment;
import com.yaz.cm.vertx.persistence.entity.Building;
import com.yaz.cm.vertx.util.DateUtil;
import com.yaz.cm.vertx.util.RxUtil;
import com.yaz.cm.vertx.util.SqlUtil;
import di.TestComponent;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.json.Json;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.mysqlclient.MySQLBatchException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import mongo.MongoApartment;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(VertxExtension.class)
class Migration {

  private static TestComponent component;

  @BeforeAll
  static void setComponent() {
    component = TestComponent.provides();
  }

  @BeforeEach
  void deploy_verticle(VertxTestContext testContext) {
    component.verticleDeployer().deploy().onComplete(testContext.succeedingThenComplete());
  }

  /*@Test
  void migrateRates() throws InterruptedException {
    final var pagingProcessor = component.ratePagingProcessorImpl();

    final var rateRepository = component.rateRepository();

    final var testObserver = RxUtil.paging(pagingProcessor, list -> {
      return rateRepository.fullSave(list)
          .doOnSuccess(rows -> {

            log.info("ROWS {} {}", rows.rowCount(), rows.size());
            rows.iterator().forEachRemaining(row -> {
              log.info("ROW {}", row.toJson().encodePrettily());
            });


          })
          .ignoreElement();
    }).doOnError(throwable -> {
      if (throwable instanceof MySQLBatchException) {
        ((MySQLBatchException) throwable).getIterationError().forEach((integer, throwable1) -> {
          log.error("ERROR {}", integer, throwable1);
        });
      }

    }).test();

    testObserver.await(5, TimeUnit.MINUTES);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }*/

  @Test
  void migrateBackups() throws InterruptedException, IOException {

    final var buildingRepository = component.buildingRepository();
    final var apartmentRepository = component.apartmentRepository();

    final var path = "/home/yaz/Downloads/cm-backup.tar.gz";
    final var temPath = "tmp/";
    final var tempPaths = Paths.get(temPath);

    try (var stream = Files.walk(tempPaths)) {

      stream
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }

    Files.createDirectories(tempPaths);

    final var archiveInputStream = new TarArchiveInputStream(
        new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(path))));

    final var pagingJsonFile = component.pagingJsonFile();

    ArchiveEntry entry;
    while ((entry = archiveInputStream.getNextTarEntry()) != null) {
      final var name = entry.getName();
      log.info(name);

      final var fileName = temPath + name;
      try (OutputStream o = Files.newOutputStream(new File(fileName).toPath())) {
        IOUtils.copy(archiveInputStream, o);
      }

      switch (name) {
        case "buildings.json.gz":

          pagingJsonFile.pagingJsonFile(30, fileName, Building.class, list -> {

            return Observable.fromIterable(list)
                .map(building -> building.toBuilder()
                    .createdAt(Optional.ofNullable(building.createdAt()).orElseGet(DateUtil::utcLocalDateTime))
                    .build())
                .toList()
                .flatMap(buildingRepository::save)
                .doOnSuccess(SqlUtil::print)
                .ignoreElement();

          }).blockingSubscribe();
          break;
        case "apartments.json.gz":

          pagingJsonFile.pagingJsonFile(30, fileName, MongoApartment.class, list -> {

            return Observable.fromIterable(list)
                .map(apt -> Apartment.builder()
                    .buildingId(apt.apartmentId().buildingId())
                    .number(apt.apartmentId().number())
                    .name(apt.name())
                    .aliquot(apt.amountToPay())
                    .emails(apt.emails())
                    .createdAt(DateUtil.utcLocalDateTime())
                    .build())
                .toList()
                .flatMap(apartmentRepository::replace)
                .doOnSuccess(SqlUtil::print)
                .doOnError(throwable -> {
                  log.info("ERROR {}", Json.encode(list));
                })
                .ignoreElement();

          }).blockingSubscribe();
          break;
      }
    }
  }


}
