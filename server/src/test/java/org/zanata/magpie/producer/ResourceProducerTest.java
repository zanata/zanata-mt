/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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

package org.zanata.magpie.producer;

import org.infinispan.manager.CacheContainer;
import org.junit.Test;
import org.mockito.Mockito;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.zanata.magpie.producer.ResourceProducer.REPLICATE_CACHE;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ResourceProducerTest {

    @Test
    public void testCacheContainer() {
        CacheContainer container = Mockito.mock(CacheContainer.class);
        ResourceProducer producer = new ResourceProducer();
        producer.setWebCacheManager(container);
        producer.initialPasswordCache();
        verify(container).getCache(REPLICATE_CACHE);
    }

    @Test
    public void testEntityManager() {
        EntityManager entityManager = Mockito.mock(EntityManager.class);
        ResourceProducer producer = new ResourceProducer();
        producer.setEntityManager(entityManager);
        assertThat(producer.getEntityManager()).isEqualTo(entityManager);
    }
}
