package io.qpointz.mill.data.backend.grpc

import io.grpc.Server
import io.qpointz.mill.data.backend.grpc.config.GrpcServerProperties
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import java.util.concurrent.TimeUnit

/**
 * Starts and stops the grpc-java [Server] together with the Spring application context.
 *
 * Uses only bind address, port, in-process name, and shutdown grace from [GrpcServerProperties].
 * `external-host` is discovery metadata only and is not read here.
 *
 * @param server     native gRPC server bean (not started until [start])
 * @param properties bind address, listen port, in-process name, and shutdown grace period
 */
class GrpcServerLifecycle(
    private val server: Server,
    private val properties: GrpcServerProperties,
) : SmartLifecycle {

    private val log = LoggerFactory.getLogger(javaClass)

    @Volatile
    private var running = false

    override fun start() {
        server.start()
        running = true
        val port = server.port
        if (port > 0) {
            log.info("gRPC server listening on {}:{}", properties.host, port)
        } else if (properties.inProcessName.isNotBlank()) {
            log.info("gRPC in-process server name={}", properties.inProcessName)
        } else {
            log.info("gRPC server started (port={})", port)
        }
    }

    override fun stop() {
        stop { }
    }

    override fun stop(callback: Runnable) {
        Thread {
            try {
                server.shutdown()
                if (!server.awaitTermination(properties.shutdownGraceSeconds.toLong(), TimeUnit.SECONDS)) {
                    server.shutdownNow()
                    server.awaitTermination(5, TimeUnit.SECONDS)
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                server.shutdownNow()
            } finally {
                running = false
                callback.run()
            }
        }.start()
    }

    override fun isRunning(): Boolean = running
}
