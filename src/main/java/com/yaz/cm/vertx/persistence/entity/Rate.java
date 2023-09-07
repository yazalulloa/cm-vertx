package com.yaz.cm.vertx.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.yaz.cm.vertx.domain.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
@EqualsAndHashCode
public class Rate {

  @JsonProperty
  private final Long id;

  @JsonProperty
  private final Currency fromCurrency;

  @JsonProperty
  private final Currency toCurrency;

  @JsonProperty
  private final BigDecimal rate;

  @JsonProperty
  private final LocalDate dateOfRate;

  @JsonProperty
  private final Source source;

  @JsonProperty
  private final LocalDateTime createdAt;

  @JsonProperty
  private final String description;

  @JsonProperty
  private final Long hash;

  @JsonProperty
  private final String etag;

  @JsonProperty
  private final String lastModified;


  public enum Source {
    BCV, PLATFORM;

    public static final Source[] values = values();
  }
}
