package io.qpointz.rapids.services.flight;

import io.qpointz.rapids.calcite.CalciteHandler;
import io.qpointz.rapids.services.AbstractBackgroundService;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.apache.arrow.flight.FlightServer;
import org.apache.arrow.flight.Location;
import org.apache.arrow.memory.RootAllocator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;

@ApplicationScoped
@Slf4j
public class FlightService extends AbstractBackgroundService {

    private final FlightServiceConfig serviceConfig;

    public FlightService(Vertx vertx, FlightServiceConfig serviceConfig) {
        super(vertx, serviceConfig);
        this.serviceConfig = serviceConfig;
    }

    @Inject
    CalciteHandler calciteHandler;

    @Override
    protected String serviceName() {
        return "Flight GRPC";
    }

    @Override
    protected String configurationPrefix() {
        return FlightServiceConfig.configurationPrefix;
    }

    @Override
    protected void startService() {

        final var allocator = new RootAllocator();
        final var location = Location.forGrpcInsecure(this.serviceConfig.host(), this.serviceConfig.port());
        var builder = FlightServer.builder()
                .allocator(allocator)
                .location(location)
                .producer(new CalciteFlightProducer(calciteHandler));

        try (var flightServer = builder.build()) {
            log.info("Flight server is about to launch");
            flightServer.start();
            log.info("Flight server started");
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
