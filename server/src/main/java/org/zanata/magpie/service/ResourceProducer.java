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

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.zanata.magpie.annotation.ClusteredCache;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class ResourceProducer {

    @Resource(lookup = "java:jboss/infinispan/container/web")
    private CacheContainer webCacheManager;

    static final String REPLICATE_CACHE = "repl";


    @Produces
    @ClusteredCache(REPLICATE_CACHE)
    public Cache<String, String> initialPasswordCache() {
        // this cache is defined in the standalone-openshift.xml
        return webCacheManager.getCache(REPLICATE_CACHE);
    }

}
