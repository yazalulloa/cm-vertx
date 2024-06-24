package com.yaz.cm.vertx.vertx;

import com.yaz.cm.vertx.util.Constants;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.impl.Utils;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheTemplateHandler implements TemplateHandler {

  private static final Set<String> PATHS_TO_CACHE;

  static {
    PATHS_TO_CACHE = Stream.of("/buildings-selector")
        .flatMap(str -> Stream.of(str, str + ".html"))
        .collect(Collectors.toSet());
  }

  private final Vertx vertx;
  private final TemplateEngine engine;
  private final String templateDirectory;
  private final String contentType;
  private String indexTemplate;

  public CacheTemplateHandler(Vertx vertx, TemplateEngine engine, String templateDirectory, String contentType) {
    this.vertx = vertx;
    this.engine = engine;
    this.templateDirectory = templateDirectory == null || templateDirectory.isEmpty() ? "." : templateDirectory;
    this.contentType = contentType;
    this.indexTemplate = DEFAULT_INDEX_TEMPLATE;
  }


  private String cachedKey(String path) {
    return PATHS_TO_CACHE.stream().filter(path::endsWith).findFirst().orElse(null);
  }

  public void preHandler(RoutingContext context) {

    final var cacheKey = cachedKey(context.request().path());
    if (cacheKey == null) {
      context.next();
      return;
    }

    final var tmpRoute = String.format("/%s%s%s", Constants.TEMPLATE_TMP_DIR, cacheKey,
        cacheKey.endsWith(".html") ? "" : ".html");

    vertx.fileSystem().exists(Constants.STATIC_DIR + tmpRoute)
        .onSuccess(b -> {
          if (b) {
            //log.info("CACHE_HIT {}", tmpRoute);
            context.reroute(tmpRoute);

          } else {
            context.put("cache_key", cacheKey);
            context.put("cache_tmp_route", tmpRoute);
            context.next();
          }

        })
        .onFailure(context::fail);

  }

  @Override
  public void handle(RoutingContext context) {
    final var file = file(context);

    if (!context.request().isEnded()) {
      context.request().pause();
    }

    // log.info("FILE {}", file);

    final var cacheKey = context.get("cache_key");

    if (cacheKey == null) {
      render(context, file);
      return;
    }

    // log.info("CACHE_KEY {}", cacheKey);
    render(context, file);
  }

  private String file(RoutingContext context) {
    String file = Utils.pathOffset(context.normalizedPath(), context);
    if (file.endsWith("/") && null != indexTemplate) {
      file += indexTemplate;
    }
    // files are always normalized (start with /)
    // however if there's no base strip / to avoid making the path absolute
    if (templateDirectory == null || templateDirectory.isEmpty()) {
      // strip the leading slash from the filename
      file = file.substring(1);
    }
    // put the locale if present and not on the context yet into the context.
    if (!context.data().containsKey("lang")) {
      for (LanguageHeader acceptableLocale : context.acceptableLanguages()) {
        try {
          Locale.forLanguageTag(acceptableLocale.value());
        } catch (RuntimeException e) {
          // we couldn't parse the locale so it's not valid or unknown
          continue;
        }
        context.data().put("lang", acceptableLocale.value());
        break;
      }
    }

    return file;
  }

  private void render(RoutingContext context, String file) {
    engine.render(new JsonObject(context.data()), templateDirectory + file, res -> {
      if (res.succeeded()) {
        if (!context.request().isEnded()) {
          context.request().resume();
        }

        final String tmpRoute = context.get("cache_tmp_route");
        if (tmpRoute != null) {
          final var tempPath = Constants.STATIC_DIR + "/" + Constants.TEMPLATE_TMP_DIR;
          final var cachedPath = Constants.STATIC_DIR + tmpRoute;

          vertx.fileSystem().mkdirs(tempPath)
              .flatMap(v -> vertx.fileSystem().writeFile(cachedPath, res.result()))
              .onSuccess(v -> context.reroute(tmpRoute))
              .onFailure(context::fail);
        } else {
          context.response().putHeader(HttpHeaders.CONTENT_TYPE, contentType).end(res.result());
        }
      } else {
        if (!context.request().isEnded()) {
          context.request().resume();
        }
        context.fail(res.cause());
      }
    });
  }

  @Override
  public TemplateHandler setIndexTemplate(String indexTemplate) {
    this.indexTemplate = indexTemplate;
    return this;
  }
}
