package com.yaz.cm.vertx.di;

import com.yaz.cm.vertx.verticle.BcvRateVerticle;
import com.yaz.cm.vertx.verticle.GoogleVerticle;
import com.yaz.cm.vertx.verticle.HttpClientVerticle;
import com.yaz.cm.vertx.verticle.HttpServerVerticle;
import com.yaz.cm.vertx.verticle.JobVerticle;
import com.yaz.cm.vertx.verticle.MySqlVerticle;
import com.yaz.cm.vertx.verticle.TelegramVerticle;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Verticle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Named;
import javax.inject.Provider;

@Module
public class VerticleModule {

  public static final String CONFIG_VERTICLES = "configVerticles";
  public static final String CONFIG_SINGLE_VERTICLES = "configSingleVerticles";


  @Provides
  @Named(CONFIG_VERTICLES)
  public static List<Map.Entry<String, Provider<? extends Verticle>>> configVerticles(
      Provider<HttpServerVerticle> httpServerVerticleProvider,
      Provider<HttpClientVerticle> httpClientVerticleProvider,
      Provider<BcvRateVerticle> bcvRateVerticleProvider,
      Provider<TelegramVerticle> telegramVerticleProvider,
      Provider<GoogleVerticle> googleVerticleProvider
  ) {

    final var list = new ArrayList<Entry<String, Provider<? extends Verticle>>>();
    list.add(Map.entry("http_server", httpServerVerticleProvider));
    list.add(Map.entry("http_client", httpClientVerticleProvider));
    list.add(Map.entry("bcv_rate_config", bcvRateVerticleProvider));
    list.add(Map.entry("telegram_config", telegramVerticleProvider));
    list.add(Map.entry("google_config", googleVerticleProvider));
    list.add(Map.entry("mysql-db", MySqlVerticle::new));
    //list.add(Map.entry("mongodb", MongoVerticle::new));

    return list;
  }


  @Provides
  @Named(CONFIG_SINGLE_VERTICLES)
  public static List<Map.Entry<String, Provider<? extends Verticle>>> configSingleVerticles(
      Provider<JobVerticle> jobVerticleProvider) {
    final var list = new ArrayList<Entry<String, Provider<? extends Verticle>>>();
    list.add(Map.entry("jobs", jobVerticleProvider));
    return list;
  }
}
