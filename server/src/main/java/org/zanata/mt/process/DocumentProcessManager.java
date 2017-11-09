package org.zanata.mt.process;

import com.google.common.annotations.VisibleForTesting;
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
import org.infinispan.util.concurrent.locks.StripedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.LocaleCode;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import java.util.function.Supplier;

/**
 * Important: This lock does not support clustering.
 *
 * Handle locking of api request for
 * {@link org.zanata.mt.api.service.DocumentResource#translate(DocumentContent, LocaleCode)}
 *
 * Limit to process a {document + source language + target language}
 * translation request at a time. See {@link DocumentProcessKey}
 *
 * TODO: Implement clustering lock
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class DocumentProcessManager {
    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentProcessManager.class);

//    private final StripedLock lock = new StripedLock();

    @Inject
    private EmbeddedCacheManager cacheManager;

    private Cache<DocumentProcessKey, Boolean> docProcessCache;

    private TransactionManager tm;

    @SuppressWarnings("unused")
    public DocumentProcessManager() {
    }

    @PostConstruct
    public void init() {
        docProcessCache = cacheManager.getCache("docProcessCache");
        tm = docProcessCache.getAdvancedCache().getTransactionManager();
    }

    private void lock(@NotNull DocumentProcessKey key)
            throws SystemException, NotSupportedException,
            HeuristicRollbackException, HeuristicMixedException,
            RollbackException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Locking document translation request:{}", key.toString());
        }
        tm.begin();
        docProcessCache.getAdvancedCache().lock(key);
        docProcessCache.put(key, true);
        tm.commit();
//        lock.acquireLock(key, true);
    }

    private void unlock(@NotNull DocumentProcessKey key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("release document translation lock:{}", key.toString());
        }
        docProcessCache.remove(key);
//        lock.releaseLock(key);
    }

    public Response withLock(DocumentProcessKey key,
            Supplier<Response> function) {
        try {
            lock(key);
            return function.get();
        } catch (Exception e) {
            LOG.error("Unable to lock request process:" + key);
            return Response.serverError().build();
        } finally {
            unlock(key);
        }
    }

    @VisibleForTesting
    protected int getTotalLockCount() {
        return docProcessCache.size();
//        lock.getTotalLockCount();
    }

    @Produces
    @ApplicationScoped
    public EmbeddedCacheManager defaultClusteredCacheManager() {
        GlobalConfiguration g = new GlobalConfigurationBuilder()
                .clusteredDefault()
                .transport()
                .clusterName("MachineTranslationsCluster")
                .build();
        Configuration cfg = new ConfigurationBuilder()
                .clustering()
                .cacheMode(CacheMode.DIST_ASYNC)
                .eviction()
                .strategy(EvictionStrategy.LRU)
                .type(EvictionType.COUNT).size(150)
                .transaction()
                .transactionMode(TransactionMode.TRANSACTIONAL)
                .lockingMode(LockingMode.PESSIMISTIC)
                .build();
        return new DefaultCacheManager(g, cfg);
    }
}
