package org.zanata.mt.process;

import com.google.common.annotations.VisibleForTesting;
import org.infinispan.util.concurrent.locks.StripedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.LocaleCode;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotNull;

/**
 * Important: This lock uses single thread lock library which does not support
 * clustering.
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

    private final StripedLock lock = new StripedLock();

    @SuppressWarnings("unused")
    public DocumentProcessManager() {
    }

    public void lock(@NotNull DocumentProcessKey key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Locking document translation request:{}", key.toString());
        }
        lock.acquireLock(key, true);
    }

    public void unlock(@NotNull DocumentProcessKey key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("release document translation lock:{}", key.toString());
        }
        lock.releaseLock(key);
    }

    @VisibleForTesting
    protected int getTotalLockCount() {
        return lock.getTotalLockCount();
    }
}
