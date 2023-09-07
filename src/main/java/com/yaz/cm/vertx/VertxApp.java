package com.yaz.cm.vertx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.launcher.VertxCommandLauncher;
import io.vertx.core.impl.launcher.VertxLifecycleHooks;
import io.vertx.core.json.JsonObject;

public class VertxApp extends VertxCommandLauncher implements VertxLifecycleHooks {

    public static void main(String[] args) {
        new VertxApp().dispatch(args);
    }

    /**
     * Utility method to execute a specific command.
     *
     * @param cmd  the command
     * @param args the arguments
     */
    public static void executeCommand(String cmd, String... args) {
        new Launcher().execute(cmd, args);
    }

    /**
     * Hook for sub-classes of {@link Launcher} after the config has been parsed.
     *
     * @param config the read config, empty if none are provided.
     */
    public void afterConfigParsed(JsonObject config) {
    }

    /**
     * Hook for sub-classes of {@link Launcher} before the vertx instance is started.
     *
     * @param options the configured Vert.x options. Modify them to customize the Vert.x instance.
     */
    public void beforeStartingVertx(VertxOptions options) {

    }

    /**
     * Hook for sub-classes of {@link Launcher} after the vertx instance is started.
     *
     * @param vertx the created Vert.x instance
     */
    public void afterStartingVertx(Vertx vertx) {

    }

    /**
     * Hook for sub-classes of {@link Launcher} before the verticle is deployed.
     *
     * @param deploymentOptions the current deployment options. Modify them to customize the deployment.
     */
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {

    }

    @Override
    public void beforeStoppingVertx(Vertx vertx) {

    }

    @Override
    public void afterStoppingVertx() {

    }

    /**
     * A deployment failure has been encountered. You can override this method to customize the behavior.
     * By default it closes the `vertx` instance.
     *
     * @param vertx             the vert.x instance
     * @param mainVerticle      the verticle
     * @param deploymentOptions the verticle deployment options
     * @param cause             the cause of the failure
     */
    public void handleDeployFailed(Vertx vertx, String mainVerticle, DeploymentOptions deploymentOptions, Throwable cause) {
        // Default behaviour is to close Vert.x if the deploy failed
        vertx.close();
    }
}
