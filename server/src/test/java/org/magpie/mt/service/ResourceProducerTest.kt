package org.magpie.mt.service

import org.assertj.core.api.Assertions.assertThat
import org.infinispan.manager.EmbeddedCacheManager
import org.junit.After
import org.junit.Before
import org.junit.Test

class ResourceProducerTest {

    private lateinit var resourceProducer: ResourceProducer

    @Before
    fun setUp() {
        resourceProducer = ResourceProducer()
    }

    private val cacheManager: EmbeddedCacheManager? by lazy {
        val cacheManager = resourceProducer.defaultClusteredCacheManager()
        cacheManager
    }

    @After
    fun cleanUp() {
        cacheManager?.stop()
    }

    @Test
    fun canGetCacheManager() {
        assertThat(cacheManager?.clusterName).isEqualTo(ResourceProducer.MACHINE_TRANSLATIONS_CLUSTER)
        assertThat(cacheManager?.cacheManagerConfiguration?.isClustered).isTrue()
    }

    @Test
    fun canGetCache() {
        val docProcessCache = resourceProducer.docProcessCache(cacheManager)
        assertThat(docProcessCache).isNotNull
        assertThat(docProcessCache.size).isEqualTo(0)
    }

    @Test
    fun canGetTransactionManager() {
        val transactionManager = resourceProducer.cacheTransactionManager(resourceProducer.docProcessCache(cacheManager))
        // in test environment there is a dummy transaction manager
        assertThat(transactionManager).isNotNull()
    }

}
