/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.mt.cache;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.concurrent.TimeUnit;

/**
 * Produces a cache container for injection.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class CacheManagerProducer {

    private DefaultCacheManager manager;

    @SuppressWarnings("unused")
    public CacheManagerProducer() {
    }

    @Produces
    @ApplicationScoped
    public DefaultCacheManager getCacheManager() {
        if (manager == null) {
            GlobalConfiguration globalConfiguration =
                    GlobalConfigurationBuilder.defaultClusteredBuilder()
                            .globalJmxStatistics().allowDuplicateDomains(true)
                            .build();
            ConfigurationBuilder config = new ConfigurationBuilder();
            config.clustering().cacheMode(CacheMode.DIST_SYNC).locking()
                    .useLockStriping(false)
                    .lockAcquisitionTimeout(1, TimeUnit.HOURS);
            manager = new DefaultCacheManager(globalConfiguration,
                    config.build());
        }
        return manager;
    }

    @PreDestroy
    public void cleanUp() {
        manager.stop();
        manager = null;
    }
}
