package com.yaz.cm.vertx.di;

import com.yaz.cm.vertx.vertx.VerticleDeployer;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class, VertxModule.class, BindsModule.class, VerticleModule.class, GoogleModule.class})
public interface ApplicationComponent {

  VerticleDeployer verticleDeployer();
}
