package com.yaz.cm.vertx.vertx;

import com.yaz.cm.vertx.di.VerticleModule;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerticleDeployerImpl implements VerticleDeployer {


  private final Vertx vertx;
  private final List<Entry<String, Provider<? extends Verticle>>> configVerticles;
  private final List<Map.Entry<String, Provider<? extends Verticle>>> configSingleVerticles;

  @Inject
  public VerticleDeployerImpl(Vertx vertx,
      @Named(VerticleModule.CONFIG_VERTICLES) List<Entry<String, Provider<? extends Verticle>>> configVerticles,
      @Named(VerticleModule.CONFIG_SINGLE_VERTICLES) List<Entry<String, Provider<? extends Verticle>>> configSingleVerticles) {
    this.vertx = vertx;
    this.configVerticles = configVerticles;
    this.configSingleVerticles = configSingleVerticles;
  }


  /*private final Provider<HttpServerVerticle> httpServerVerticleProvider;
  private final Provider<HttpClientVerticle> httpClientVerticleProvider;
  private final Provider<BcvRateVerticle> bcvRateVerticleProvider;
  private final Provider<TelegramVerticle> telegramVerticleProvider;
  private final Provider<JobVerticle> jobVerticleProvider;


  private Map<String, Supplier<Verticle>> verticles() {

    final var verticles = new HashMap<String, Supplier<Verticle>>();
    verticles.put("mysql-db", MySqlVerticle::new);
    verticles.put("mongodb", MongoVerticle::new);
    verticles.put("jobs", jobVerticleProvider::get);
    verticles.put("http_server", httpServerVerticleProvider::get);
    verticles.put("http_client", httpClientVerticleProvider::get);
    verticles.put("bcv_rate_config", bcvRateVerticleProvider::get);
    verticles.put("telegram_config", telegramVerticleProvider::get);
    return verticles;
  }*/

  public Future<?> deploy() {

    final var fileStore = new ConfigStoreOptions()
        .setType("file")
        .setFormat("yaml")
        .setConfig(new JsonObject()
            .put("path", "config/config.yml")
        );

    final var configRetrieverOptions = new ConfigRetrieverOptions()
        .addStore(fileStore)
        .setScanPeriod(5000);

    final var retriever = ConfigRetriever.create(vertx, configRetrieverOptions);

    return retriever.getConfig()
        .map(config -> {

          final var instances = Integer.getInteger("vertx.loop_size", 1);

          return Stream.concat(
              deployStream(config, configVerticles, instances),
              deployStream(config, configSingleVerticles, 1)
          ).toList();
        })
        .flatMap(Future::all);
  }

  private Stream<Future<String>> deployStream(JsonObject config, List<Entry<String, Provider<? extends Verticle>>> verticles,
      int instances) {

    return verticles.stream()
        .map(entry -> {
          final var jsonObject = config.getJsonObject(entry.getKey());

          if (jsonObject != null) {
            final var deploymentOptions = new DeploymentOptions().setConfig(jsonObject)
                .setInstances(instances);

            return vertx.deployVerticle(entry.getValue()::get, deploymentOptions);

          } else {
            log.info("Verticle {} not found in config", entry.getKey());
            return Future.succeededFuture();
          }
        });
  }
}
