package com.yaz.cm.vertx.di;

import com.yaz.cm.vertx.service.http.HttpService;
import com.yaz.cm.vertx.service.http.HttpServiceImpl;
import com.yaz.cm.vertx.vertx.VerticleDeployer;
import com.yaz.cm.vertx.vertx.VerticleDeployerImpl;
import dagger.Binds;
import dagger.Module;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

@Module
public interface BindsModule {

  @Binds
  VerticleDeployer bindsVerticleDeployer(VerticleDeployerImpl verticleDeployer);

  @Binds
  TemplateEngine bindsTemplateEngine(ThymeleafTemplateEngine templateEngine);


  @Binds
  HttpService bindsHttpService(HttpServiceImpl httpService);
}
