package com.yaz.cm.vertx.service;

import com.yaz.cm.vertx.service.http.HttpService;
import com.yaz.cm.vertx.util.SystemUtil;
import com.yaz.cm.vertx.vertx.VertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.MetricsService;
import java.util.Map;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SystemInfoService {

  private final VertxHandler vertxHandler;
  private final HttpService httpService;
  private final MetricsService metricsService;


  public Single<Map<String, Object>> info() {
    return httpService.requestCount()
        .map(requestCount -> {

          final var httpRequestCount = "HTTP REQUEST COUNT %s".formatted(requestCount);
          final var list = SystemUtil.systemInfoList();
          list.add(httpRequestCount);

          final var metricsSnapshot = metricsService.getMetricsSnapshot();

          log.info("METRICS\n {}", metricsSnapshot.size());

            return Map.of("system_info", list);
        });
  }
}
