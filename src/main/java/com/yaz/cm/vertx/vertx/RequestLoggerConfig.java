package com.yaz.cm.vertx.vertx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
public class RequestLoggerConfig {

  @Builder.Default
  @JsonProperty("request_log_config")
  private final HttpLogConfig requestLogConfig = HttpLogConfig.builder().build();

  @Builder.Default
  @JsonProperty("response_log_config")
  private final HttpLogConfig responseLogConfig = HttpLogConfig.builder().build();

}
