package com.yaz.cm.vertx.vertx;

import io.vertx.core.Future;

public interface VerticleDeployer {

  Future<?> deploy();
}
