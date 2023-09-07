package com.yaz.cm.vertx;

import com.yaz.cm.vertx.persistence.entity.Rate;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.common.template.TemplateEngine;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(onConstructor_={@Inject})
public class RatesTemplateEngine {

    private final TemplateEngine templateEngine;


    public Future<Buffer> rates(List<Rate> rates, Map<String, Object> appConfig) {
        final var list = rates.stream()
                .map(Json::encode)
                .map(JsonObject::new)
                .map(JsonObject::getMap)
                .toList();

        final var data = new HashMap<String, Object>();
        data.put("rates", list);
        data.put("first_page_url", "/api/rates");
        data.put("previous_page_url", "/api/rates");
        data.put("next_page_url", "/api/rates");
        data.put("last_page_url", "/api/rates");
        data.putAll(appConfig);


        return templateEngine.render(data, "rates");
    }

    public void clearCache() {
        templateEngine.clearCache();
    }
}
