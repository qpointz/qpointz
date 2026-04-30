package io.qpointz.mill.persistence.ai.jpa

import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshStateRepository
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaValueMappingRefreshStateAdapter
import io.qpointz.mill.persistence.ai.jpa.repositories.AiValueMappingStateJpaRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Registers [ValueMappingRefreshStateRepository] when JPA metadata and `ai_value_mapping_state` are present (WI-184).
 */
@AutoConfiguration
@ConditionalOnClass(AiValueMappingStateJpaRepository::class)
class AiV3ValueMappingStateAutoConfiguration {

    @Bean
    @ConditionalOnBean(AiValueMappingStateJpaRepository::class)
    @ConditionalOnMissingBean(ValueMappingRefreshStateRepository::class)
    fun valueMappingRefreshStateRepository(
        jpa: AiValueMappingStateJpaRepository,
    ): ValueMappingRefreshStateRepository = JpaValueMappingRefreshStateAdapter(jpa)
}
