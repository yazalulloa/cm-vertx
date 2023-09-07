package com.yaz.cm.vertx.vertx;

import com.yaz.cm.vertx.util.JacksonUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.tracing.opentelemetry.OpenTelemetryOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxProvider {

  public static Vertx vertx() {

    final var micrometerMetricsOptions = new MicrometerMetricsOptions()
        .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
        .setEnabled(true);

    final var sdkTracerProvider = SdkTracerProvider.builder().build();
    final var openTelemetry = OpenTelemetrySdk.builder()
        .setTracerProvider(sdkTracerProvider)
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .buildAndRegisterGlobal();

    final var options = new VertxOptions()
        .setMetricsOptions(micrometerMetricsOptions)
        .setTracingOptions(new OpenTelemetryOptions(openTelemetry))
        .setPreferNativeTransport(true)   ;

    final var loopPoolSize = options.getEventLoopPoolSize();
    log.info("CORES {}", loopPoolSize);
    System.setProperty("vertx.loop_size", String.valueOf(loopPoolSize));

    final var vertx = Vertx.vertx(options);

    return configureVertx(vertx);
  }

  public static Vertx configureVertx(Vertx vertx) {
    JacksonUtil.defaultConfig(DatabindCodec.mapper());
    JacksonUtil.defaultConfig(DatabindCodec.prettyMapper());

    final var eventBus = vertx.eventBus();
    final var defaultJacksonMessageCodec = new DefaultJacksonMessageCodec();
    eventBus.registerCodec(defaultJacksonMessageCodec);
    eventBus.codecSelector(body -> defaultJacksonMessageCodec.name());

    return vertx;
  }
}
