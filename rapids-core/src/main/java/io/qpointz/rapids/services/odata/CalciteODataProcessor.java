package io.qpointz.rapids.services.odata;

import io.qpointz.rapids.calcite.CalciteHandler;
import jdk.jshell.spi.ExecutionControl;
import lombok.SneakyThrows;
import org.apache.calcite.tools.RelBuilder;
import org.apache.olingo.commons.api.data.*;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.ServiceDocumentProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CalciteODataProcessor implements EntityCollectionProcessor, EntityProcessor, ServiceDocumentProcessor {
    private final OData odata;

    private final String schemaName;

    private final ServiceMetadata serviceMetadata;
    private final CalciteHandler calciteHandler;

    public CalciteODataProcessor(OData odata, ServiceMetadata edm, String schemaName, CalciteHandler calcite) {
        this.odata = odata;
        this.serviceMetadata = edm;
        this.schemaName = schemaName;
        this.calciteHandler = calcite;
    }

    @Override
    public void readServiceDocument(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        final var serializer = this.odata
                .createSerializer(responseFormat);

        final var result = serializer.serviceDocument(this.serviceMetadata,request.getRawBaseUri());

        response.setContent(result.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        final var edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());

        final var serializer = this.odata
                .createSerializer(responseFormat);

        final ExpandOption expand = uriInfo.getExpandOption();
        final SelectOption select = uriInfo.getSelectOption();

        final var serviceRoot = request.getRawBaseUri();
        final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();

        final var serializationOptions = EntityCollectionSerializerOptions.with()
                .id(id)
                .contextURL(isODataMetadataNone(responseFormat) ? null : getContextUrl(edmEntitySet, false, expand, select, null, URI.create(serviceRoot+"/")))
                .count(uriInfo.getCountOption())
                .expand(expand).select(select)
                .build();

        EntityIterator entitySet = null;

        try {
            entitySet = toEntityIterator(uriInfo);
        } catch (SQLException e) {
            throw new ODataApplicationException("SQL Exception thrown: %s".formatted(e.getMessage()), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH, e);
        }


        final var serializedContent = serializer
                .entityCollectionStreamed(this.serviceMetadata, edmEntitySet.getEntityType(), entitySet, serializationOptions);


        response.setODataContent(serializedContent.getODataContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    private ContextURL getContextUrl(final EdmEntitySet entitySet, final boolean isSingleEntity,
                                     final ExpandOption expand, final SelectOption select, final String navOrPropertyPath, final URI serviceRoot)
            throws SerializerException {
        return ContextURL.with().entitySet(entitySet)
                .serviceRoot(serviceRoot)
                .selectList(odata.createUriHelper().buildContextURLSelectList(entitySet.getEntityType(), expand, select))
                .suffix(isSingleEntity ? ContextURL.Suffix.ENTITY : null)
                .navOrPropertyPath(navOrPropertyPath)
                .build();
    }

    private EdmEntitySet getEdmEntitySet(final UriInfoResource uriInfo) throws ODataApplicationException {
        final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        /*
         * To get the entity set we have to interpret all URI segments
         */
        if (!(resourcePaths.get(0) instanceof UriResourceEntitySet)) {
            throw new ODataApplicationException("Invalid resource type for first segment.",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }

        /*
         * Here we should interpret the whole URI but in this example we do not support navigation so we throw an exception
         */

        final UriResourceEntitySet uriResource = (UriResourceEntitySet) resourcePaths.get(0);
        return uriResource.getEntitySet();
    }

    public static boolean isODataMetadataNone(final ContentType contentType) {
        return contentType.isCompatible(ContentType.APPLICATION_JSON)
                && ContentType.VALUE_ODATA_METADATA_NONE.equalsIgnoreCase(
                contentType.getParameter(ContentType.PARAMETER_ODATA_METADATA));
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
    }

    private EntityIterator toEntityIterator(UriInfo uriInfo) throws ODataApplicationException, SQLException {
        final var edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());

        final var builder = RelBuilder.create(this.calciteHandler.getFrameworkConfig());
        final var queryNode = builder
                .scan(this.schemaName, edmEntitySet.getName())
                .build();
        var statement = this.calciteHandler.getRelRunner().prepareStatement(queryNode);
        var resultSet = statement.executeQuery();
        final var rsMeta = resultSet.getMetaData();
        final var columns = new HashMap<Integer, String>();
        for(int i=1;i<= rsMeta.getColumnCount();i++) {
            columns.put(i, rsMeta.getColumnName(i));
        }

        return new EntityIterator() {

            private boolean gotNext = false;
            private boolean hasNext = false;
            @SneakyThrows
            @Override
            public boolean hasNext() {
                if (!gotNext) {
                    hasNext = resultSet.next();
                    gotNext = true;
                }
                return hasNext;
            }

            @SneakyThrows
            @Override
            public Entity next() {
                if (!gotNext) {
                    resultSet.next();
                }
                this.gotNext = false;
                var e = new Entity();
                for (var kv : columns.entrySet()) {
                    var p = new Property(null, kv.getValue(), ValueType.PRIMITIVE, resultSet.getObject(kv.getKey()));
                    e.addProperty(p);
                }
                return e;
            }
        };

    }


}
