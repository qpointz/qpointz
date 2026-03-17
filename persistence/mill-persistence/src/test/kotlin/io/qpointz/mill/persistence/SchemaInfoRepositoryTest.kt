package io.qpointz.mill.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class SchemaInfoRepositoryTest {

    @Autowired
    lateinit var repository: SchemaInfoRepository

    @Test
    fun `should persist and reload a schema info entity`() {
        val entity = SchemaInfoEntity(lane = "chat-memory", description = "Chat memory lane")
        val saved = repository.save(entity)

        assertThat(saved.id).isGreaterThan(0)
        assertThat(repository.findById(saved.id)).isPresent
    }

    @Test
    fun `should store multiple lanes independently`() {
        repository.save(SchemaInfoEntity(lane = "events", description = "Event lane"))
        repository.save(SchemaInfoEntity(lane = "artifacts", description = "Artifact lane"))

        val all = repository.findAll()
        assertThat(all.map { it.lane }).contains("events", "artifacts")
    }
}
