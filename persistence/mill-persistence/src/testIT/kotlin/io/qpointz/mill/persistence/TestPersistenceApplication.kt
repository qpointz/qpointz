package io.qpointz.mill.persistence

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EntityScan(basePackages = ["io.qpointz.mill.persistence"])
@EnableJpaRepositories(basePackages = ["io.qpointz.mill.persistence"])
class TestPersistenceApplication
