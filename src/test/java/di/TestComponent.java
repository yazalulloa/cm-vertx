package di;

import com.yaz.cm.vertx.MongoService;
import com.yaz.cm.vertx.PagingDataTemplateService;
import com.yaz.cm.vertx.PropertiesLoader;
import com.yaz.cm.vertx.RateMongoService;
import com.yaz.cm.vertx.RatePagingProcessorImpl;
import com.yaz.cm.vertx.controller.RatesController;
import com.yaz.cm.vertx.di.AppModule;
import com.yaz.cm.vertx.di.BindsModule;
import com.yaz.cm.vertx.di.GoogleModule;
import com.yaz.cm.vertx.di.VertxModule;
import com.yaz.cm.vertx.persistence.repository.ApartmentRepository;
import com.yaz.cm.vertx.persistence.repository.BuildingRepository;
import com.yaz.cm.vertx.persistence.repository.RateRepository;
import com.yaz.cm.vertx.service.ApartmentService;
import com.yaz.cm.vertx.service.RateService;
import com.yaz.cm.vertx.util.PagingJsonFile;
import com.yaz.cm.vertx.vertx.VerticleDeployer;
import com.yaz.cm.vertx.vertx.VertxHandler;
import com.yaz.cm.vertx.vertx.VertxProvider;
import dagger.Component;
import io.vertx.core.Vertx;
import io.vertx.ext.web.common.template.TemplateEngine;
import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class, VertxModule.class, BindsModule.class, TestVerticleModule.class,
    GoogleModule.class})
public interface TestComponent {

  VerticleDeployer verticleDeployer();

  PropertiesLoader propertiesLoader();

  PagingDataTemplateService templateService();

  RatePagingProcessorImpl ratePagingProcessorImpl();

  Vertx vertx();

  VertxHandler vertxHandler();

  RateMongoService rateMongoService();

  RateService rateService();

  PagingJsonFile pagingJsonFile();

  BuildingRepository buildingRepository();
  ApartmentRepository apartmentRepository();

  RateRepository rateRepository();

  TemplateEngine templateEngine();

  RatesController ratesController();

  MongoService mongoService();
  ApartmentService apartmentService();

  static TestComponent provides() {
    final var vertx = VertxProvider.vertx();

    return DaggerTestComponent.builder()
        .vertxModule(new VertxModule(vertx))
        .appModule(new AppModule("assets/i18n"))
        .build();
  }
}
