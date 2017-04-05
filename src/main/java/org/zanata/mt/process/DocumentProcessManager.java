package org.zanata.mt.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.LocaleId;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotNull;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handle locking of api request for
 * {@link org.zanata.mt.api.service.DocumentContentTranslatorResource#translate(DocumentContent, LocaleId)}
 *
 * Limit to process one translation request at a time.
 * This lock uses single thread lock library which does not support clustering.
 *
 * TODO: To support clustering
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class DocumentProcessManager {
    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentProcessManager.class);

    private final ReentrantLock lock = new ReentrantLock(true);

    @SuppressWarnings("unused")
    public DocumentProcessManager() {
    }

    public void lock(@NotNull DocumentProcessKey key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Locking document translation request." +
                    key.toString());
        }
        lock.lock();
    }

    public void unlock(@NotNull DocumentProcessKey key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("release document translation lock." + key.toString());
        }
        lock.unlock();
    }

    public boolean isLocked(@NotNull DocumentProcessKey key) {
        return lock.isLocked();
    }

    public ReentrantLock getLock() {
        return lock;
    }
}
