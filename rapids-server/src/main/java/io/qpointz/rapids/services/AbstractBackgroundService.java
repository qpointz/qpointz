package io.qpointz.rapids.services;

import io.qpointz.rapids.ServerUtils;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;


@Slf4j
public abstract class AbstractBackgroundService {

    private final WorkerExecutor executor;
    private final AbstractBackgroundServiceConfig serviceConfig;

    protected WorkerExecutor executor() {
        return this.executor;
    }

    protected abstract String serviceName();

    protected abstract String configurationPrefix();

    private AbstractBackgroundService() {
        this(null, null);
    }
    public AbstractBackgroundService(Vertx vertx, AbstractBackgroundServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
        this.executor = vertx.createSharedWorkerExecutor(serviceConfig.worker().name(),
                                                         serviceConfig.worker().poolSize());
    }

    public void onStart(@Observes StartupEvent ev) {
        log.info("{} Service init", this.serviceName());
    }

    public void onStop(@Observes ShutdownEvent ev) {
        log.info("{} server shut down", this.serviceName());
    }

    @PostConstruct
    public void postConstruct() {
        log.info("{} Service configuration", this.serviceName());
        ServerUtils.logConfig(log, this.configurationPrefix());

        if (!this.serviceConfig.enabled()) {
            log.info("{} server disabled. No action to be taken", this.serviceName());
            return;
        }

        this.executor().executeBlocking(p -> {
            log.info("{} service about to start.", this.serviceName());
            this.startService();
        }).onSuccess(p -> {
            log.info("{} service started", this.serviceName());
        }).onFailure(p-> {
            log.error(String.format("%s service failed to start", this.serviceName()),p);
        }).onComplete(p -> {
            log.info("{} server start completed", this.serviceName());
        });


    }

    protected abstract void startService();

}
