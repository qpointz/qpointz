package io.qpointz.mill.data.backend;

import io.qpointz.mill.data.backend.configuration.DefaultServiceConfiguration;
import io.qpointz.mill.data.backend.metadata.configuration.MetadataConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;

@Slf4j
@AutoConfiguration
public class LegacyAutoconfiguration {

    @Autowired
    MetadataConfiguration metadataConfiguration;

    @Autowired
    DefaultServiceConfiguration serviceConfiguration;

//    @Autowired
//    DefaultFilterChainConfiguration filterChainConfiguration;

    //@Autowired
    //PolicyConfiguration policyConfiguration;

}
