package com.yaz.cm.vertx.di;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaz.cm.vertx.PropertiesLoader;
import com.yaz.cm.vertx.RouterProvider;
import com.yaz.cm.vertx.util.TemplateUtils;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import javax.inject.Singleton;

@Module()
public class AppModule {

  private final String templatePropertiesDir;

  public AppModule(String templatePropertiesDir) {
    this.templatePropertiesDir = templatePropertiesDir;
  }

  @Provides
  @Singleton
  PropertiesLoader propertiesLoader(io.vertx.rxjava3.core.Vertx vertx) {
    return new PropertiesLoader(templatePropertiesDir, vertx);
  }

  @Provides
  @Singleton
  ThymeleafTemplateEngine thymeleafTemplateEngine(io.vertx.core.Vertx vertx) {
    final var engine = ThymeleafTemplateEngine.create(vertx);
    TemplateUtils.configureThymeleafEngine(engine);
    return engine;
  }

  @Provides
  @Singleton
  Router providesRouter(RouterProvider routerProvider) {
    return routerProvider.router();
  }

  @Provides
  static ObjectMapper providesObjectMapper() {
    return DatabindCodec.mapper();
  }
}
