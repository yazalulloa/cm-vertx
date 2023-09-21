package com.yaz.cm.vertx.util;

import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

public class TemplateUtils {

  public static void configureThymeleafEngine(ThymeleafTemplateEngine engine) {

    final var templateResolver = new FileTemplateResolver();
    templateResolver.setPrefix(Constants.TEMPLATE_PREFIX);
    templateResolver.setSuffix(Constants.TEMPLATE_SUFFIX);
    ((TemplateEngine) engine.unwrap()).setTemplateResolver(templateResolver);
    //engine.getThymeleafTemplateEngine().setMessageResolver(new WebAppMessageResolver());

       /* ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix(Constants.TEMPLATE_PREFIX);
        templateResolver.setSuffix(Constants.TEMPLATE_SUFFIX);
        engine.getThymeleafTemplateEngine().setTemplateResolver(templateResolver);

        CustomMessageResolver customMessageResolver = new CustomMessageResolver();
        engine.getThymeleafTemplateEngine().setMessageResolver(customMessageResolver);*/
  }
}
