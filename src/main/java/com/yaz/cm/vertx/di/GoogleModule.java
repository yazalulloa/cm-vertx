package com.yaz.cm.vertx.di;

import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.providers.GoogleAuth;
import javax.inject.Singleton;

@Module
public abstract class GoogleModule {

  @Provides
  @Singleton
  static JsonFactory providesJsonFactory() {
    return GsonFactory.getDefaultInstance();
  }

  @Provides
  @Singleton
  static HttpTransport providesHttpTransport() {
    try {
      return GoogleNetHttpTransport.newTrustedTransport();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Provides
  @Singleton
  static GooglePublicKeysManager providesGooglePublicKeysManager(HttpTransport transport, JsonFactory jsonFactory) {
    return new GooglePublicKeysManager(transport, jsonFactory);
  }

  @Provides
  @Singleton
  static OAuth2Auth providesGoogleAuth(Vertx vertx) {
    final var googleClientId = System.getenv("GOOGLE_CLIENT_ID");
    final var googleClientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
    return GoogleAuth.create(vertx, googleClientId, googleClientSecret);
  }
}
