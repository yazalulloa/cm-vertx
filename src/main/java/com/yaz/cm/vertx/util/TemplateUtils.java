package com.yaz.cm.vertx.util;

import com.yaz.cm.vertx.WebAppMessageResolver;
import com.yaz.cm.vertx.util.Constants;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

public class TemplateUtils {

    public static void configureThymeleafEngine(ThymeleafTemplateEngine engine) {

        final var templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix(Constants.TEMPLATE_PREFIX);
        templateResolver.setSuffix(Constants.TEMPLATE_SUFFIX);
        engine.getThymeleafTemplateEngine().setTemplateResolver(templateResolver);
        engine.getThymeleafTemplateEngine().setMessageResolver(new WebAppMessageResolver());

       /* ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix(Constants.TEMPLATE_PREFIX);
        templateResolver.setSuffix(Constants.TEMPLATE_SUFFIX);
        engine.getThymeleafTemplateEngine().setTemplateResolver(templateResolver);

        CustomMessageResolver customMessageResolver = new CustomMessageResolver();
        engine.getThymeleafTemplateEngine().setMessageResolver(customMessageResolver);*/
    }
}
