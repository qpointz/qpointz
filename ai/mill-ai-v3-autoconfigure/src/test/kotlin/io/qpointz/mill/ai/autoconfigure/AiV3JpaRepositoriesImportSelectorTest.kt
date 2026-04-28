package io.qpointz.mill.ai.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.type.AnnotationMetadata

class AiV3JpaRepositoriesImportSelectorTest {

    @Test
    fun skipsDuplicateRepositoryConfigurationWhenPersistenceAutoConfigurationOnClasspath() {
        val imports = AiV3JpaRepositoriesImportSelector()
            .selectImports(AnnotationMetadata.introspect(AiV3JpaConfiguration::class.java))
        assertThat(imports).isEmpty()
    }
}
