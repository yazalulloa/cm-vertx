package com.yaz.cm.vertx;

import com.yaz.cm.vertx.di.AppModule;
import com.yaz.cm.vertx.di.DaggerApplicationComponent;
import com.yaz.cm.vertx.di.VertxModule;
import com.yaz.cm.vertx.util.Constants;
import com.yaz.cm.vertx.util.ConvertUtil;
import com.yaz.cm.vertx.util.EnvUtil;
import com.yaz.cm.vertx.util.FileUtil;
import com.yaz.cm.vertx.util.NetworkUtil;
import com.yaz.cm.vertx.vertx.VertxProvider;
import io.vertx.core.Vertx;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

  public static void main(String[] args) throws IOException {
    log.info("STARTING");
    EnvUtil.saveAppStartedAt();
    //NetworkUtil.showPublicIp();

    //Files.createDirectories(Paths.get("config"));
    //FileUtil.writeEnvToFile("CONFIG_FILE", "config/config.yml");
    // Files.deleteIfExists(Paths.get("%s/%s".formatted(Constants.STATIC_DIR, Constants.TEMPLATE_TMP_DIR)));

    if (EnvUtil.isShowDir()) {
      FileUtil.showDir();
    }

    new Application().run(args);
  }

  private void run(String[] args) {
    run(VertxProvider.vertx(), args);
  }

  public void run(Vertx vertx, String[] args) {

    final var applicationComponent = DaggerApplicationComponent.builder()
        .vertxModule(new VertxModule(vertx))
        .appModule(new AppModule("assets/i18n"))
        .build();

    vertx.fileSystem().deleteRecursive("%s/%s".formatted(Constants.STATIC_DIR, Constants.TEMPLATE_TMP_DIR), true);

    applicationComponent.verticleDeployer().deploy()
        .onSuccess(v -> {
          final var appStartedAt = EnvUtil.getAppStartedAt();
          final var duration = ConvertUtil.formatDuration(appStartedAt);

          log.info("STARTED {}ms", System.currentTimeMillis() - appStartedAt);

        })
        .onFailure(t -> {
          log.error("Error deploying verticles", t);
          System.exit(-1);
        });
  }

}
