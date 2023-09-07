package com.yaz.cm.vertx.verticle;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.yaz.cm.vertx.domain.internal.Result;
import com.yaz.cm.vertx.domain.internal.error.ResponseError;
import com.yaz.cm.vertx.util.RandomUtil;
import com.yaz.cm.vertx.vertx.BaseVerticle;
import dagger.Lazy;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class GoogleVerticle extends BaseVerticle {

  public static final String GET_SIGN_IN_CONFIG = address("get-sign-in-config");
  public static final String VERIFY_TOKEN = address("verify-jwt-token");

  private final Lazy<GooglePublicKeysManager> publicKeysManager;

  private static GoogleIdTokenVerifier tokenVerifier;

  @Override
  public void start() throws Exception {
    eventBusFunction(VERIFY_TOKEN, this::verifyToken);
    eventBusSupplier(GET_SIGN_IN_CONFIG, this::getSignInConfig);
  }

  private JsonObject getSignInConfig() {
    return config()
        .put("nonce", RandomUtil.randomIntStr(config().getInteger("nonce_size")))
        .put("login_uri", System.getenv("ORIGIN") + System.getenv("GOOGLE_CALLBACK_URL"))
        .put("client_id", System.getenv("GOOGLE_CLIENT_ID"));
  }

  private GoogleIdTokenVerifier tokenVerifier() {
    if (tokenVerifier != null) {
      return tokenVerifier;
    }

    synchronized (this) {
      if (tokenVerifier != null) {
        return tokenVerifier;
      }

      tokenVerifier = new GoogleIdTokenVerifier.Builder(publicKeysManager.get())
          .setAudience(Collections.singleton(System.getenv("GOOGLE_CLIENT_ID")))
          .build();

      return tokenVerifier;
    }
  }


  public Result<JsonObject, ResponseError> verifyToken(String str) throws GeneralSecurityException, IOException {

    final var idToken = tokenVerifier().verify(str);

    if (idToken != null) {
      final var payload = idToken.getPayload();
      return Result.success(new JsonObject(Json.encode(payload)));
    } else {
      return Result.error(ResponseError.forbidden("invalid idToken"));
    }

  }
}
