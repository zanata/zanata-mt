/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.magpie.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.transaction.TransactionManager;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.zanata.magpie.annotation.ClusteredCache;
import org.zanata.magpie.process.DocumentProcessKey;

import static org.zanata.magpie.process.DocumentProcessManager.DOC_PROCESS_CACHE;
import static org.zanata.magpie.service.MTStartup.INITIAL_PASSWORD_CACHE;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class ResourceProducer {

    @Produces
    @ApplicationScoped
    public EmbeddedCacheManager defaultClusteredCacheManager() {
        // http://infinispan.org/docs/9.1.x/user_guide/user_guide.html#which_cache_mode_should_i_use
        GlobalConfiguration g = new GlobalConfigurationBuilder()
                .clusteredDefault()
                .transport()
                .clusterName("web")
                .globalJmxStatistics()
//                .allowDuplicateDomains(true)
                .build();
        Configuration cfg = new ConfigurationBuilder()
                .clustering()
                .cacheMode(CacheMode.REPL_SYNC)
//                .hash()
//                .numOwners(2)
                .eviction()
                .strategy(EvictionStrategy.LRU)
                .type(EvictionType.COUNT).size(150)
                .transaction()
                .transactionMode(TransactionMode.TRANSACTIONAL)
                .lockingMode(LockingMode.PESSIMISTIC)
                .build();
        return new DefaultCacheManager(g, cfg);
    }

    @Produces
    @ClusteredCache(DOC_PROCESS_CACHE)
    public Cache<DocumentProcessKey, Boolean> docProcessCache(
            EmbeddedCacheManager cacheManager) {
        return cacheManager.getCache(DOC_PROCESS_CACHE);
    }

    @Produces
    @ClusteredCache(DOC_PROCESS_CACHE)
    public TransactionManager dockProcessCacheTransactionManager(
            @ClusteredCache(DOC_PROCESS_CACHE)
                    Cache<DocumentProcessKey, Boolean> cache) {
        return cache.getAdvancedCache().getTransactionManager();
    }

    @Produces
    @ClusteredCache(INITIAL_PASSWORD_CACHE)
    public Cache<String, String> initialPasswordCache(
            EmbeddedCacheManager cacheManager) {
        return cacheManager.getCache("repl");
    }

    @Produces
    @ClusteredCache(INITIAL_PASSWORD_CACHE)
    public TransactionManager initialPasswordCacheTransactionManager(
            @ClusteredCache(INITIAL_PASSWORD_CACHE)
                    Cache<String, String> cache) {
        return cache.getAdvancedCache().getTransactionManager();
    }
}
