package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.metadata.domain.MetadataEntity;
import io.qpointz.mill.metadata.domain.facet.FacetAssignment;
import io.qpointz.mill.metadata.repository.FacetReadSide;
import io.qpointz.mill.metadata.service.MetadataEntityService;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@EnableAutoConfiguration
@AutoConfigurationPackage
@ComponentScan(basePackages = {"io.qpointz"})
public class NlSqlChatServiceTestApp {

    @Bean
    public MetadataEntityService metadataEntityService() {
        return new MetadataEntityService() {
            @Override
            public MetadataEntity findById(String id) {
                return null;
            }

            @Override
            public java.util.List<MetadataEntity> findAll() {
                return java.util.List.of();
            }

            @Override
            public java.util.List<MetadataEntity> findByKind(String kind) {
                return java.util.List.of();
            }

            @Override
            public MetadataEntity create(MetadataEntity entity, String actor) {
                throw new UnsupportedOperationException("metadataEntityService is a test stub");
            }

            @Override
            public MetadataEntity update(MetadataEntity entity, String actor) {
                throw new UnsupportedOperationException("metadataEntityService is a test stub");
            }

            @Override
            public void delete(String id, String actor) {
                throw new UnsupportedOperationException("metadataEntityService is a test stub");
            }
        };
    }

    @Bean
    public FacetReadSide facetReadSide() {
        return new FacetReadSide() {
            @Override
            public java.util.List<FacetAssignment> findByEntity(String entityId) {
                return java.util.List.of();
            }

            @Override
            public java.util.List<FacetAssignment> findByEntityAndType(String entityId, String facetTypeKey) {
                return java.util.List.of();
            }

            @Override
            public java.util.List<FacetAssignment> findByEntityTypeAndScope(String entityId, String facetTypeKey, String scopeKey) {
                return java.util.List.of();
            }

            @Override
            public FacetAssignment findByUid(String uid) {
                return null;
            }

            @Override
            public int countByFacetType(String facetTypeKey) {
                return 0;
            }
        };
    }
}

