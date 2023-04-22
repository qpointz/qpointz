package io.qpointz.rapids.server.worker;

import io.qpointz.rapids.services.odata.CalciteEdmProvider;
import io.qpointz.rapids.services.odata.CalciteODataProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ServiceMetadata;
import org.eclipse.jetty.servlet.ServletHolder;

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

    private ODataServlet(SchemaPlus schema, String namespace) {
        this.schema = schema;
        this.namespace = namespace;
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            OData odata = OData.newInstance();
            ServiceMetadata edm = odata.createServiceMetadata(new CalciteEdmProvider(this.namespace, this.schema), new ArrayList<>());
            final var handler = odata.createHandler(edm);
            handler.register(new CalciteODataProcessor(odata, schema, namespace, edm));
            handler.process(req, resp);
        } catch (RuntimeException e) {
            log.error("Server Error", e);
            throw new ServletException(e);
        }
    }

    public static ServletHolder create(SchemaPlus schema, String namespace) {
        return new ServletHolder(new ODataServlet(schema, namespace));
    }

}
