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
package org.zanata.magpie.service;

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.annotation.TopologyChanged;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachelistener.event.TopologyChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Listener
public class CacheListener {
    private static final Logger log =
            LoggerFactory.getLogger(CacheListener.class);

    @TopologyChanged
    public void observeTopologyChange(TopologyChangedEvent<String, String> event) {
        if (!event.isPre()) {
            log.info("++++++ Cache {} topology changed, new membership is {}", event.getCache().getName(), event.getConsistentHashAtEnd().getMembers());


        }

    }

    @CacheEntryCreated
    public void observeAdd(CacheEntryCreatedEvent<String, String> event) {
        if (event.isPre())
            return;

        log.info("Cache entry {} added in cache {}", event.getKey(), event.getCache());
    }

    @CacheEntryModified
    public void observeUpdate(CacheEntryModifiedEvent<String, String> event) {
        if (event.isPre())
            return;

        log.info("Cache entry {} = {} modified in cache {}", event.getKey(), event.getValue(), event.getCache());
    }

    @CacheEntryRemoved
    public void observeRemove(CacheEntryRemovedEvent<String, String> event) {
        if (event.isPre())
            return;

        log.info("Cache entry {} removed in cache {}", event.getKey(), event.getCache());
    }
}
