package di;

import com.yaz.cm.vertx.di.VerticleModule;
import com.yaz.cm.vertx.verticle.BcvRateVerticle;
import com.yaz.cm.vertx.verticle.GoogleVerticle;
import com.yaz.cm.vertx.verticle.HttpClientVerticle;
import com.yaz.cm.vertx.verticle.HttpServerVerticle;
import com.yaz.cm.vertx.verticle.MongoVerticle;
import com.yaz.cm.vertx.verticle.MySqlVerticle;
import com.yaz.cm.vertx.verticle.TelegramVerticle;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Verticle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Named;
import javax.inject.Provider;

@Module
public abstract class TestVerticleModule {


  @Provides
  @Named(VerticleModule.CONFIG_VERTICLES)
  static List<Entry<String, Provider<? extends Verticle>>> providesConfigVerticles(
      //Provider<HttpServerVerticle> httpServerVerticleProvider,
      Provider<HttpClientVerticle> httpClientVerticleProvider,
      Provider<BcvRateVerticle> bcvRateVerticleProvider,
      Provider<TelegramVerticle> telegramVerticleProvider,
      Provider<GoogleVerticle> googleVerticleProvider
  ) {

    final var list = new ArrayList<Entry<String, Provider<? extends Verticle>>>();
    //list.add(Map.entry("http_server", httpServerVerticleProvider));
    list.add(Map.entry("http_client", httpClientVerticleProvider));
    list.add(Map.entry("bcv_rate_config", bcvRateVerticleProvider));
    list.add(Map.entry("telegram_config", telegramVerticleProvider));
    list.add(Map.entry("google_config", googleVerticleProvider));
    list.add(Map.entry("mysql-db", MySqlVerticle::new));
    list.add(Map.entry("mongodb", MongoVerticle::new));

    return list;
  }

  @Provides
  @Named(VerticleModule.CONFIG_SINGLE_VERTICLES)
  static List<Entry<String, Provider<? extends Verticle>>> providesConfigSingleVerticles() {
    return Collections.emptyList();
  }
}
