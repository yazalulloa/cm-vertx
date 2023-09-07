package com.yaz.cm.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SimpleAuthenticationProvider implements AuthenticationProvider {


  @Override
  public Future<User> authenticate(JsonObject credentials) {
    log.info("authenticate(JsonObject credentials) {}", credentials);
    return AuthenticationProvider.super.authenticate(credentials);
  }

  @Override
  public void authenticate(Credentials credentials, Handler<AsyncResult<User>> resultHandler) {
    log.info("authenticate(Credentials credentials, Handler<AsyncResult<User>> resultHandler) {}", credentials);
    AuthenticationProvider.super.authenticate(credentials, resultHandler);
  }

  @Override
  public Future<User> authenticate(Credentials credentials) {
    log.info("authenticate(Credentials credentials) {}", credentials);
    return AuthenticationProvider.super.authenticate(credentials);
  }

  @Override
  public void authenticate(JsonObject credentials, Handler<AsyncResult<User>> resultHandler) {
    log.info("authenticate(JsonObject credentials, Handler<AsyncResult<User>> resultHandler) {}", credentials);
    resultHandler.handle(Future.failedFuture("Not implemented"));
  }
}
