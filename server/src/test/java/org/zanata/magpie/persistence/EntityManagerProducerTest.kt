package org.zanata.magpie.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito
import javax.persistence.EntityManager

class EntityManagerProducerTest {
    @Test
    fun canProduceEntityManager() {
        val entityManagerProducer = EntityManagerProducer()
        val em = Mockito.mock(EntityManager::class.java)
        entityManagerProducer.entityManager = em
        assertThat(entityManagerProducer.entityManager).isSameAs(em)
    }
}
