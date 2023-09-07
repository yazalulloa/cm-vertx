package com.yaz.cm.vertx.verticle;


import com.cronutils.model.CronType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.noenv.cronutils.CronScheduler;
import com.yaz.cm.vertx.verticle.service.bcv.SaveNewBcvRate;
import com.yaz.cm.vertx.vertx.BaseVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class JobVerticle extends BaseVerticle {

  private final SaveNewBcvRate saveNewBcvRate;

  @Override
  public void start() throws Exception {
    config().getJsonArray("jobs")
        .forEach(jobJson -> {
          final var jobConfig = (JsonObject) jobJson;

          final var job = Json.decodeValue(jobConfig.encode(), Job.class);

          CronScheduler.create(vertx, job.cron, job.type)
              .schedule(s -> {

                resolveJob(job.name, job.address);
              });
        });
  }

  public void resolveJob(String name, String address) {

    switch (address) {
      case "bcv-rate-job" -> {
        final var completable = saveNewBcvRate.saveNewRate().ignoreElement();
        subscribe(completable);
      }
      default -> {
        log.info("NO JOB CONFIGURED FOR {} {}", name, address);
      }
    }
  }

  @Jacksonized
  @Builder(toBuilder = true)
  @Accessors(fluent = true)
  @ToString
  @Getter
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  @AllArgsConstructor
  @EqualsAndHashCode
  private static class Job {

    @JsonProperty
    private final String name;
    @JsonProperty
    private final String cron;
    @JsonProperty
    private final CronType type;
    @JsonProperty
    private final String address;

  }
}
