package com.yaz.cm.vertx.controller;

import io.reactivex.rxjava3.core.Single;
import io.vertx.ext.web.RoutingContext;
import java.util.Map;

public interface DataContextProvider {

  Single<Map<String, Object>> data(RoutingContext ctx);
}
