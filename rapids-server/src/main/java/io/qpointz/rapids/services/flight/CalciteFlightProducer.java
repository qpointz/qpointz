package io.qpointz.rapids.services.flight;

import io.qpointz.rapids.calcite.CalciteHandler;
import org.apache.arrow.flight.*;

public class CalciteFlightProducer implements FlightProducer {

    private final CalciteHandler calcite;

    public CalciteFlightProducer(CalciteHandler handler) {
        this.calcite = handler;
    }

    @Override
    public void getStream(CallContext context, Ticket ticket, ServerStreamListener listener) {
        throw new RuntimeException("Not Implemented Yet");
    }

    @Override
    public void listFlights(CallContext context, Criteria criteria, StreamListener<FlightInfo> listener) {
        throw new RuntimeException("Not Implemented Yet");
    }

    @Override
    public FlightInfo getFlightInfo(CallContext context, FlightDescriptor descriptor) {
        throw new RuntimeException("Not Implemented Yet");
    }

    @Override
    public Runnable acceptPut(CallContext context, FlightStream flightStream, StreamListener<PutResult> ackStream) {
        throw new RuntimeException("Not Implemented Yet");
    }

    @Override
    public void doAction(CallContext context, Action action, StreamListener<Result> listener) {
        throw new RuntimeException("Not Implemented Yet");
    }

    @Override
    public void listActions(CallContext context, StreamListener<ActionType> listener) {
        throw new RuntimeException("Not Implemented Yet");
    }
}
