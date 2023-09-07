package com.yaz.cm.vertx.di;

import com.yaz.cm.vertx.vertx.VertxHandler;
import com.yaz.cm.vertx.vertx.VertxHandlerImpl;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

@Module
public class VertxModule {

    private final Vertx vertx;
    private final io.vertx.rxjava3.core.Vertx rxVertx;

    public VertxModule(Vertx vertx) {

        this.vertx = vertx;
        this.rxVertx = io.vertx.rxjava3.core.Vertx.newInstance(this.vertx);
    }

    @Provides
    public Vertx providesVertx() {
        return vertx;
    }

    @Provides
    public EventBus providesEventBus() {
        return vertx.eventBus();
    }

    @Provides
    public io.vertx.rxjava3.core.Vertx providesRxVertx() {
        return rxVertx;
    }

    @Provides
    public VertxHandler providesVertxHandler() {
        return new VertxHandlerImpl(vertx);
    }
}
