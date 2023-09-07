package com.yaz.cm.vertx.vertx;

import com.yaz.cm.vertx.util.StringUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerFormatter;
import io.vertx.ext.web.handler.LoggerHandler;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestLogger implements RequestLogHandler {

  private static final AtomicLong COUNTER = new AtomicLong(0);

  private final Vertx vertx;
  private final boolean immediate;

  private Function<HttpServerRequest, String> customFormatter;
  private final LoggerFormat format;

  private final AtomicReference<RequestLoggerConfig> atomicReference = new AtomicReference<>(
      RequestLoggerConfig.builder().build());

  public RequestLogger(Vertx vertx, boolean immediate, LoggerFormat format) {
    this.vertx = vertx;
    this.immediate = immediate;
    this.format = format;

    vertx.setPeriodic(TimeUnit.SECONDS.toMillis(5), l -> update());
  }

  public RequestLogger(LoggerFormat format, Vertx vertx) {
    this(vertx, false, format);
  }

  public String body(Buffer body) {

    try {
      return body.toJsonObject().encodePrettily();
    } catch (Exception e) {
      try {
        return body.toJsonArray().encodePrettily();
      } catch (Exception e2) {
        //return null;
      }
    }

    return "";
  }

  private void update() {
   /* vertx.eventBus().<RequestLoggerConfig>request(RequestLoggerConfigVerticle.GET_CONFIG, null)
        .map(Message::body)
        .onSuccess(atomicReference::set);*/
  }

  public void handle(RoutingContext context) {

    try {
      update();

      //final var now = ZonedDateTime.now(DateUtil.VE_ZONE);
      final var now = ZonedDateTime.now();
      final var date = now.toString();
      long timestamp = now.toInstant().toEpochMilli();

      final var request = context.request();
      final var httpMethod = request.method();
      final var headers = request.headers();

      final var requestCounter = COUNTER.addAndGet(1);

      final var builder = new StringBuilder("\n").append(String.format("[HTTP_SERVER_REQUEST_%s]", requestCounter))
          .append("\n");

      builder.append(requestCounter).append(" > ").append(httpMethod).append(" ").append(request.absoluteURI())
          .append("\n")
          .append(requestCounter).append(" > ").append(httpMethod).append(" ").append(request.uri()).append("\n")
          .append(requestCounter).append(" > ").append(request.version()).append(" ").append(date).append("\n");

      headers.forEach(entry -> builder.append(requestCounter).append(" > ").append(entry.getKey()).append(": ")
          .append(entry.getValue()).append("\n"));

      Optional.ofNullable(request.remoteAddress())
          .ifPresent(socketAddress -> {
           /* builder.append(requestCounter).append(" > ").append("remote_address_host").append(" ")
                .append(socketAddress.host()).append("\n")
                .append(requestCounter).append(" > ").append("remote_address_host_address").append(" ")
                .append(socketAddress.hostAddress()).append("\n");*/
          });

      Optional.ofNullable(request.localAddress())
          .ifPresent(socketAddress -> {
           /* builder.append(requestCounter).append(" > ").append("local_address_host").append(" ")
                .append(socketAddress.host()).append("\n")
                .append(requestCounter).append(" > ").append("local_address_host_address").append(" ")
                .append(socketAddress.hostAddress()).append("\n");*/
          });

      Optional.ofNullable(request.cookies())
          .filter(s -> !s.isEmpty())
          .map(set -> {

            return set.stream()
                .map(cookie -> {
                  final var domain = cookie.getDomain();
                  final var name = cookie.getName();
                  final var path = cookie.getPath();
                  final var maxAge = cookie.getMaxAge();
                  final var value = cookie.getValue();
                  final var sameSite = cookie.getSameSite();
                  final var httpOnly = cookie.isHttpOnly();
                  final var secure = cookie.isSecure();
                  final var encode = cookie.encode();

                  return "DOMAIN: " + domain + " "
                      + "NAME: " + name + " "
                      + "PATH: " + path + " "
                      + "MAX_AGE: " + maxAge + " "
                      + "VALUE: " + value + " "
                      + "SAME_SITE: " + sameSite + " "
                      + "HTTP_ONLY: " + httpOnly + " "
                      + "SECURE: " + secure + " "
                      + "ENCODE: " + encode;
                })
                .collect(Collectors.joining(", "));
          })
          .ifPresent(str -> builder.append(requestCounter).append(" > ").append("COOKIES").append(" ").append(str)
              .append("\n"));

      Optional.ofNullable(context.session())
          .map(Session::data)
          .map(Objects::toString)
          .map(str -> builder.append(requestCounter).append(" > ").append("SESSION").append(" ").append(str)
              .append("\n"));

      final var isJson = Optional.ofNullable(request.getHeader("content-type"))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .map(s -> s.contains("json"))
          .orElse(false);

      final int contentLength = Optional.ofNullable(request.getHeader("content-length"))
          .map(String::trim)
          .map(StringUtil::replaceNonNumeric)
          .filter(s -> !s.isEmpty())
          .map(Integer::parseInt)
          .orElse(0);

      final var config = atomicReference.get();

      final var maxSizeBody = config.requestLogConfig().maxSizeBody();
      if (config.requestLogConfig().showBody() && contentLength > 0 && contentLength < maxSizeBody) {

        context.request().bodyHandler(buffer -> {
          builder.append(body(buffer, config.requestLogConfig(), isJson));
          log.info(builder.append("\n").toString());
        });

      } else if (config.requestLogConfig().showBody() && contentLength >= maxSizeBody) {
        builder.append("BODY EXCEEDS LIMIT OF ").append(maxSizeBody);
        log.info(builder.append("\n").toString());
      } else {
        log.info(builder.append("\n").toString());
      }

      context.addEndHandler((handler) -> {

        final var response = request.response();

        final var responseBody = response.getStatusCode() == 200
            && (request.absoluteURI().contains("report") || request.absoluteURI().contains("search")) ? ""
            : responseBody(context, config.responseLogConfig());

        final var responseLogBuilder = new StringBuilder("\n").append(
                String.format("[HTTP_SERVER_RESPONSE_%s]", requestCounter)).append("\n")
            .append(requestCounter).append(" < ").append(response.getStatusCode()).append(" ").append(httpMethod)
            .append(" ").append(request.absoluteURI()).append("\n")
            .append(requestCounter).append(" < ").append(response.getStatusCode()).append(" ").append(httpMethod)
            .append(" ").append(request.uri()).append("\n")
            .append(requestCounter).append(" < ").append(httpMethod).append(" ").append(request.uri()).append("\n")
            .append(requestCounter).append(" < ").append(request.version()).append(" ")
            .append(response.getStatusMessage()).append("\n")
            .append(requestCounter).append(" < ").append(date).append("\n")
            .append(requestCounter).append(" < ").append(System.currentTimeMillis() - timestamp).append("ms")
            .append("\n");

        headers.forEach(
            entry -> responseLogBuilder.append(requestCounter).append(" < ").append(entry.getKey()).append(": ")
                .append(entry.getValue()).append("\n"));

        Optional.ofNullable(request.remoteAddress())
            .ifPresent(socketAddress -> {
             /* responseLogBuilder.append(requestCounter).append(" < ").append("remote_address_host").append(" ")
                  .append(socketAddress.host()).append("\n")
                  .append(requestCounter).append(" < ").append("remote_address_host_address").append(" ")
                  .append(socketAddress.hostAddress()).append("\n");*/
            });

        Optional.ofNullable(request.localAddress())
            .ifPresent(socketAddress -> {
              /*responseLogBuilder.append(requestCounter).append(" < ").append("local_address_host").append(" ")
                  .append(socketAddress.host()).append("\n")
                  .append(requestCounter).append(" < ").append("local_address_host_address").append(" ")
                  .append(socketAddress.hostAddress()).append("\n");*/
            });

        Optional.ofNullable(context.session())
            .map(session -> {
              final var str = Optional.ofNullable(session.data()).map(Objects::toString).orElse("");
              return builder.append(requestCounter).append(" > ").append("SESSION ").append(session.id()).append(" ")
                  .append(str)
                  .append("\n");
            });

        responseLogBuilder.append(responseBody)
            .append("\n");

        log.info(responseLogBuilder.toString());
      });

    } catch (Exception e) {
      log.error("ERROR LOGGING", e);
    }
    context.next();
  }

  private String responseBody(RoutingContext context, HttpLogConfig config) {
    if (!config.showBody()) {
      return "[SHOW RESPONSE BODY IS DISABLED]";
    }
    final var responseBody = context.get("response_body");
    if (responseBody == null) {
      return "";
    }

    if (responseBody instanceof String || responseBody instanceof Number || responseBody instanceof Buffer) {
      return responseBody.toString();
    }

    try {
      if (config.pretty()) {
        return Json.encodePrettily(responseBody);
      } else {
        return Json.encode(responseBody);
      }

    } catch (Exception ignored) {

    }

    return responseBody.toString();

  }

  private String body(Buffer buffer, HttpLogConfig config, boolean isJson) {
    if (!config.showBody()) {
      return "[SHOW REQUEST BODY IS DISABLED]";
    }

    if (!isJson) {
      return buffer.toString();
    }

    try {
      final var jsonObject = buffer.toJsonObject();

      return config.pretty() ? jsonObject.encodePrettily() : jsonObject.encode();
    } catch (Exception ignored) {

    }

    try {
      final var jsonArray = buffer.toJsonArray();

      return config.pretty() ? jsonArray.encodePrettily() : jsonArray.encode();

    } catch (Exception ignored) {

    }

    return buffer.toString();
  }

  @Override
  public LoggerHandler customFormatter(Function<HttpServerRequest, String> formatter) {
    if (format != LoggerFormat.CUSTOM) {
      throw new IllegalStateException("Setting a formatter requires the handler to be set to CUSTOM format");
    }

    this.customFormatter = formatter;

    return this;
  }

  @Override
  public LoggerHandler customFormatter(LoggerFormatter loggerFormatter) {
    return null;
  }


}