package com.yaz.cm.vertx;

import di.TestComponent;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;


class PropertiesLoaderTest {

    @Test
    void test() throws InterruptedException {

        final var component = TestComponent.provides();

        final var propertiesLoader = component.propertiesLoader();

        final var testObserver = propertiesLoader.load()
                .doOnSuccess(j -> System.out.println(j.encodePrettily()))
                .test();

        testObserver.await(120, TimeUnit.SECONDS);

        testObserver
                .assertValue(j -> !j.isEmpty())
                .assertComplete()
                .assertNoErrors();
    }

}