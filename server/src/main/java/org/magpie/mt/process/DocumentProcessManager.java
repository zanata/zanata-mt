package org.magpie.mt.process;

import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.magpie.mt.annotation.ClusteredCache;
import org.magpie.mt.api.dto.DocumentContent;
import org.magpie.mt.api.dto.LocaleCode;

import com.google.common.annotations.VisibleForTesting;

/**
 * Important: This lock does not support clustering.
 *
 * Handle locking of api request for
 * {@link org.magpie.mt.api.service.DocumentResource#translate(DocumentContent, LocaleCode)}
 *
 * Limit to process a {document + source language + target language}
 * translation request at a time. See {@link DocumentProcessKey}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class DocumentProcessManager {
    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentProcessManager.class);
    public static final String DOC_PROCESS_CACHE = "docProcessCache";

    private Cache<DocumentProcessKey, Boolean> docProcessCache;

    private TransactionManager tm;

    @SuppressWarnings("unused")
    public DocumentProcessManager() {
    }

    @Inject
    public DocumentProcessManager(@ClusteredCache(DOC_PROCESS_CACHE) Cache<DocumentProcessKey, Boolean> cache, @ClusteredCache(DOC_PROCESS_CACHE) TransactionManager txManager) {
        docProcessCache = cache;
        tm = txManager;
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
    }

    private void unlock(@NotNull DocumentProcessKey key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("release document translation lock:{}", key.toString());
        }
        docProcessCache.remove(key);
    }

    public Response withLock(DocumentProcessKey key,
            Supplier<Response> function) {
        try {
            lock(key);
            return function.get();
        } catch (Exception e) {
            LOG.error("Unable to lock request process:" + key, e);
            return Response.serverError().build();
        } finally {
            unlock(key);
        }
    }

    @VisibleForTesting
    protected int getTotalLockCount() {
        return docProcessCache.size();
    }

}
