package com.yaz.cm.vertx.controller;

import com.yaz.cm.vertx.service.SystemInfoService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.ext.web.RoutingContext;
import java.util.Map;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AppInfoController implements DataContextProvider {

  private final SystemInfoService systemInfoService;

  public Single<Map<String, Object>> data(RoutingContext ctx) {
    return systemInfoService.info();
  }
}
