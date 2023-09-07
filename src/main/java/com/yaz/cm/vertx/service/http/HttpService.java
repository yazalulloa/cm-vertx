package com.yaz.cm.vertx.service.http;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import java.util.Map;

public interface HttpService {

  Single<Long> requestCount();

  Single<HttpClientResponse> send(HttpClientRequest request);


  default Single<HttpClientResponse> get(String url) {
    return send(HttpMethod.GET, url);
  }

  default Single<HttpClientResponse> head(String url) {
    return send(HttpMethod.HEAD, url);
  }

  default Single<HttpClientResponse> options(String url) {
    return send(HttpMethod.OPTIONS, url);
  }

  default Single<HttpClientResponse> delete(String url) {
    return send(HttpMethod.DELETE, url);
  }

  default Single<HttpClientResponse> getWithBearerAuth(String url, String token) {
    return send(HttpClientRequest.builder()
        .httpMethod(HttpMethod.GET)
        .url(url)
        .headers(Map.of(HttpHeaders.AUTHORIZATION.toString(), "Bearer " + token))
        .build());
  }

  default Single<HttpClientResponse> send(HttpMethod httpMethod, String url) {
    return send(HttpClientRequest.builder()
        .httpMethod(httpMethod)
        .url(url)
        .build());
  }

  default Single<HttpClientResponse> send(HttpMethod httpMethod, String url, Object object) {
    return send(HttpClientRequest.builder()
        .httpMethod(httpMethod)
        .url(url)
        .body(object)
        .build());
  }

  default Single<HttpClientResponse> text(HttpMethod httpMethod, String url, String object) {
    return send(HttpClientRequest.builder()
        .httpMethod(httpMethod)
        .url(url)
        .body(object)
        .mediaType(MediaType.TEXT_PLAIN)
        .build());
  }


  default Single<HttpClientResponse> json(HttpMethod httpMethod, String url, String object) {
    return send(HttpClientRequest.builder()
        .httpMethod(httpMethod)
        .url(url)
        .body(object)
        .mediaType(MediaType.APPLICATION_JSON)
        .build());
  }
}
