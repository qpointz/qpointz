package io.qpointz.rapids.services;

import io.qpointz.rapids.config.WorkerPoolConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public interface AbstractBackgroundServiceConfig {

    boolean enabled();

    WorkerPoolConfig worker();

}
