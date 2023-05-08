package io.qpointz.rapids.server.worker;

import io.qpointz.rapids.calcite.CalciteHandler;
import io.qpointz.rapids.services.odata.CalciteEdmProvider;
import io.qpointz.rapids.services.odata.CalciteODataProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ServiceMetadata;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Slf4j
public class ODataServlet extends HttpServlet {

    private final SchemaPlus schema;
    private final String namespace;
    private final CalciteHandler calciteHandler;

    private ODataServlet(SchemaPlus schema, String namespace, CalciteHandler calciteHandler) {
        this.schema = schema;
        this.namespace = namespace;
        this.calciteHandler = calciteHandler;
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            OData odata = OData.newInstance();
            ServiceMetadata edm = odata.createServiceMetadata(new CalciteEdmProvider(this.namespace, this.schema, this.calciteHandler), new ArrayList<>());
            final var handler = odata.createHandler(edm);
            handler.register(new CalciteODataProcessor(odata, edm, this.schema.getName(), this.calciteHandler));
            log.debug("Req:{}", req.getRequestURI());
            handler.process(req, resp);
        } catch (RuntimeException e) {
            log.error("Server Error", e);
            throw new ServletException(e);
        }
    }

    public static ODataServlet create(SchemaPlus schema, String namespace, CalciteHandler calciteHandler) {
        return new ODataServlet(schema, namespace, calciteHandler);
    }

}
