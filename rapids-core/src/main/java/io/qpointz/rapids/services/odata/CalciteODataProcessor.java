package io.qpointz.rapids.services.odata;

import jdk.jshell.spi.ExecutionControl;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.uri.UriInfo;

import javax.naming.OperationNotSupportedException;
import java.util.Locale;

public class CalciteODataProcessor implements EntityCollectionProcessor, EntityProcessor {
    private final OData odata;
    private final SchemaPlus schema;
    private final String namespace;
    private final ServiceMetadata serviceMetadata;

    public CalciteODataProcessor(OData odata, SchemaPlus schema, String namespace, ServiceMetadata edm) {
        this.odata = odata;
        this.schema = schema;
        this.namespace = namespace;
        this.serviceMetadata = edm;
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        throw new RuntimeException(new ExecutionControl.NotImplementedException("Not implemented readEntityCollection"));
    }

    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        throw new RuntimeException(new ExecutionControl.NotImplementedException("Not implemented readEntity"));
    }

    private static void throwNotSupported(String operationName) throws ODataApplicationException {
        throw new ODataApplicationException("'%s' operation not supported".formatted(operationName),
                                            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException {
        throwNotSupported("createEntity");
    }

    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        throwNotSupported("updateEntity");
    }

    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
        throwNotSupported("deleteEntity");
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        throw new RuntimeException(new OperationNotSupportedException("Not supported init"));
    }
}
