package com.yaz.cm.vertx;

import io.reactivex.rxjava3.core.Single;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.Vertx;

public class PropertiesLoader {

    private final ConfigRetriever retriever;


    public PropertiesLoader(String dirPath, Vertx vertx) {

        final var dir = new ConfigStoreOptions()
                .setType("directory")
                .setConfig(new JsonObject()
                        .put("path", dirPath)
                        .put("filesets", new JsonArray()
                                // .add(new JsonObject().put("pattern", "dir/*json"))
                                .add(new JsonObject()
                                        //.put("pattern", "dir/*.properties")
                                        .put("pattern", "*.properties")
                                        .put("format", "properties"))
                        ));

        final var retrieverOptions = new ConfigRetrieverOptions()
                //.setScanPeriod(2000)
                .addStore(dir)
                ;

        retriever = ConfigRetriever.create(vertx, retrieverOptions);
    }

    public Single<JsonObject> load() {
        return Single.just(new JsonObject());
        //return retriever.getConfig();
    }
}
