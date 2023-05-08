package io.qpointz.rapids.server.worker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractService extends AbstractVerticle {

    private final String name;
    private final boolean enabled;
    AbstractService(String name, boolean enabled) {
        super();
        this.name = name;
        this.enabled = enabled;
    }

    @Override
    public void start() {
        if (!this.enabled) {
            log.debug("{} server disabled. exiting", this.name);
            return;
        }

        this.vertx.executeBlocking(p -> {
            log.info("{} service about to start.", this.name);
            try {
                this.startService();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).onSuccess(p ->
            log.info("{} service started", this.name)
        ).onFailure(ex->
            log.error(String.format("%s service failed to start", this.name), ex)
        ).onComplete(p ->
            log.info("{} server start completed", this.name)
        );
    }

    protected abstract void startService() throws Exception;

    protected abstract void stopService() throws Exception;

}
