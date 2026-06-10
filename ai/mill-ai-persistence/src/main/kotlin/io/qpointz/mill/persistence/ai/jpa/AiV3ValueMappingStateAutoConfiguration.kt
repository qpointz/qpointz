package io.qpointz.mill.persistence.ai.jpa

import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshStateRepository
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaValueMappingRefreshStateAdapter
import io.qpointz.mill.persistence.ai.jpa.repositories.AiValueMappingStateJpaRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration
import org.springframework.context.annotation.Bean

/**
 * Registers [ValueMappingRefreshStateRepository] when JPA metadata and `ai_value_mapping_state` are present (WI-184).
 *
 * Runs after [DataJpaRepositoriesAutoConfiguration] so Spring Data has registered
 * [AiValueMappingStateJpaRepository] before this bean is created. Do not use
 * [org.springframework.boot.autoconfigure.condition.ConditionalOnBean] on repository types here:
 * condition evaluation can run before repository beans exist (Boot 3.5+ / 4.x ordering).
 */
@AutoConfiguration(after = [DataJpaRepositoriesAutoConfiguration::class])
@ConditionalOnClass(AiValueMappingStateJpaRepository::class)
class AiV3ValueMappingStateAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ValueMappingRefreshStateRepository::class)
    fun valueMappingRefreshStateRepository(
        jpa: AiValueMappingStateJpaRepository,
    ): ValueMappingRefreshStateRepository = JpaValueMappingRefreshStateAdapter(jpa)
}
