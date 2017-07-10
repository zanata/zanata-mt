package org.zanata.mt.process;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.CacheCollection;
import org.infinispan.CacheSet;
import org.infinispan.cache.impl.CacheImpl;
import org.infinispan.commons.api.BasicCache;
import org.infinispan.commons.util.concurrent.NotifyingFuture;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.filter.KeyFilter;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.cachelistener.filter.CacheEventConverter;
import org.infinispan.notifications.cachelistener.filter.CacheEventFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.mt.api.dto.LocaleCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.zanata.mt.process.DocumentProcessManager.DOC_LOCK_CACHE;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentProcessManagerTest {

    private DocumentProcessManager lock;

    @Mock
    private DefaultCacheManager cacheManager;

    @Before
    public void init() {
        when(cacheManager.getCache(DOC_LOCK_CACHE)).thenReturn(new BasicCache());
        lock = new DocumentProcessManager(cacheManager);
    }

    @Test
    public void testTryLock() {
        String url = "testing";
        DocumentProcessKey key =
                new DocumentProcessKey(url, LocaleCode.EN, LocaleCode.DE);
        lock.lock(key);
        assertThat(lock.isLocked(key)).isTrue();
        lock.unlock(key);
        assertThat(lock.isLocked(key)).isFalse();
    }

    private class BasicCache<DocumentProcessKey, ReentrantLock>
            implements Cache<DocumentProcessKey, ReentrantLock> {
        private Map<DocumentProcessKey, ReentrantLock> map = new HashMap<>();

        @Override
        public void putForExternalRead(DocumentProcessKey key,
                ReentrantLock value) {

        }

        @Override
        public void putForExternalRead(DocumentProcessKey key,
                ReentrantLock value,
                long lifespan, TimeUnit unit) {

        }

        @Override
        public void putForExternalRead(DocumentProcessKey key,
                ReentrantLock value,
                long lifespan, TimeUnit lifespanUnit, long maxIdle,
                TimeUnit maxIdleUnit) {

        }

        @Override
        public void evict(DocumentProcessKey key) {

        }

        @Override
        public Configuration getCacheConfiguration() {
            return null;
        }

        @Override
        public EmbeddedCacheManager getCacheManager() {
            return null;
        }

        @Override
        public AdvancedCache<DocumentProcessKey, ReentrantLock> getAdvancedCache() {
            return null;
        }

        @Override
        public ComponentStatus getStatus() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public ReentrantLock get(Object key) {
            return map.get(key);
        }

        @Override
        public CacheSet<DocumentProcessKey> keySet() {
            return null;
        }

        @Override
        public CacheCollection<ReentrantLock> values() {
            return null;
        }

        @Override
        public CacheSet<Entry<DocumentProcessKey, ReentrantLock>> entrySet() {
            return null;
        }

        @Override
        public void clear() {

        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getVersion() {
            return null;
        }

        @Override
        public ReentrantLock put(DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock) {
            return null;
        }

        @Override
        public ReentrantLock put(DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock, long l, TimeUnit timeUnit) {
            return null;
        }

        @Override
        public ReentrantLock putIfAbsent(DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock, long l, TimeUnit timeUnit) {
            return null;
        }

        @Override
        public void putAll(
                Map<? extends DocumentProcessKey, ? extends ReentrantLock> map,
                long l,
                TimeUnit timeUnit) {

        }

        @Override
        public ReentrantLock replace(DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock, long l, TimeUnit timeUnit) {
            return null;
        }

        @Override
        public boolean replace(DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock, ReentrantLock v1, long l,
                TimeUnit timeUnit) {
            return false;
        }

        @Override
        public ReentrantLock put(DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock, long l, TimeUnit timeUnit, long l1,
                TimeUnit timeUnit1) {
            return null;
        }

        @Override
        public ReentrantLock putIfAbsent(DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock, long l, TimeUnit timeUnit, long l1,
                TimeUnit timeUnit1) {
            return null;
        }

        @Override
        public void putAll(
                Map<? extends DocumentProcessKey, ? extends ReentrantLock> map,
                long l,
                TimeUnit timeUnit, long l1, TimeUnit timeUnit1) {

        }

        @Override
        public ReentrantLock replace(DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock, long l, TimeUnit timeUnit, long l1,
                TimeUnit timeUnit1) {
            return null;
        }

        @Override
        public boolean replace(DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock, ReentrantLock v1, long l,
                TimeUnit timeUnit, long l1, TimeUnit timeUnit1) {
            return false;
        }

        @Override
        public ReentrantLock remove(Object o) {
            return map.remove(o);
        }

        @Override
        public void putAll(
                Map<? extends DocumentProcessKey, ? extends ReentrantLock> m) {

        }

        @Override
        public ReentrantLock putIfAbsent(DocumentProcessKey key,
                ReentrantLock value) {
            return map.putIfAbsent(key, value);
        }

        @Override
        public boolean remove(Object key, Object value) {
            return false;
        }

        @Override
        public boolean replace(DocumentProcessKey key, ReentrantLock oldValue,
                ReentrantLock newValue) {
            return false;
        }

        @Override
        public ReentrantLock replace(DocumentProcessKey key,
                ReentrantLock value) {
            return null;
        }

        @Override
        public NotifyingFuture<ReentrantLock> putAsync(
                DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock) {
            return null;
        }

        @Override
        public NotifyingFuture<ReentrantLock> putAsync(
                DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock,
                long l, TimeUnit timeUnit) {
            return null;
        }

        @Override
        public NotifyingFuture<ReentrantLock> putAsync(
                DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock,
                long l, TimeUnit timeUnit, long l1, TimeUnit timeUnit1) {
            return null;
        }

        @Override
        public NotifyingFuture<Void> putAllAsync(
                Map<? extends DocumentProcessKey, ? extends ReentrantLock> map) {
            return null;
        }

        @Override
        public NotifyingFuture<Void> putAllAsync(
                Map<? extends DocumentProcessKey, ? extends ReentrantLock> map,
                long l,
                TimeUnit timeUnit) {
            return null;
        }

        @Override
        public NotifyingFuture<Void> putAllAsync(
                Map<? extends DocumentProcessKey, ? extends ReentrantLock> map,
                long l,
                TimeUnit timeUnit, long l1, TimeUnit timeUnit1) {
            return null;
        }

        @Override
        public NotifyingFuture<Void> clearAsync() {
            return null;
        }

        @Override
        public NotifyingFuture<ReentrantLock> putIfAbsentAsync(
                DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock) {
            return null;
        }

        @Override
        public NotifyingFuture<ReentrantLock> putIfAbsentAsync(
                DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock,
                long l, TimeUnit timeUnit) {
            return null;
        }

        @Override
        public NotifyingFuture<ReentrantLock> putIfAbsentAsync(
                DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock,
                long l, TimeUnit timeUnit, long l1, TimeUnit timeUnit1) {
            return null;
        }

        @Override
        public NotifyingFuture<ReentrantLock> removeAsync(Object o) {
            return null;
        }

        @Override
        public NotifyingFuture<Boolean> removeAsync(Object o, Object o1) {
            return null;
        }

        @Override
        public NotifyingFuture<ReentrantLock> replaceAsync(
                DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock) {
            return null;
        }

        @Override
        public NotifyingFuture<ReentrantLock> replaceAsync(
                DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock,
                long l, TimeUnit timeUnit) {
            return null;
        }

        @Override
        public NotifyingFuture<ReentrantLock> replaceAsync(
                DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock,
                long l, TimeUnit timeUnit, long l1, TimeUnit timeUnit1) {
            return null;
        }

        @Override
        public NotifyingFuture<Boolean> replaceAsync(
                DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock,
                ReentrantLock v1) {
            return null;
        }

        @Override
        public NotifyingFuture<Boolean> replaceAsync(
                DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock,
                ReentrantLock v1, long l, TimeUnit timeUnit) {
            return null;
        }

        @Override
        public NotifyingFuture<Boolean> replaceAsync(
                DocumentProcessKey documentProcessKey,
                ReentrantLock reentrantLock,
                ReentrantLock v1, long l, TimeUnit timeUnit, long l1,
                TimeUnit timeUnit1) {
            return null;
        }

        @Override
        public NotifyingFuture<ReentrantLock> getAsync(
                DocumentProcessKey documentProcessKey) {
            return null;
        }

        @Override
        public boolean startBatch() {
            return false;
        }

        @Override
        public void endBatch(boolean b) {

        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void addListener(Object listener,
                KeyFilter<? super DocumentProcessKey> filter) {

        }

        @Override
        public <C> void addListener(Object listener,
                CacheEventFilter<? super DocumentProcessKey, ? super ReentrantLock> filter,
                CacheEventConverter<? super DocumentProcessKey, ? super ReentrantLock, C> converter) {

        }

        @Override
        public void addListener(Object listener) {

        }

        @Override
        public void removeListener(Object listener) {

        }

        @Override
        public Set<Object> getListeners() {
            return null;
        }
    }
}
