package org.zanata.mt.process;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.mt.api.dto.LocaleCode;

import javax.transaction.TransactionManager;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.zanata.mt.process.DocumentProcessManager.DOC_PROCESS_CACHE;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocumentProcessManagerTest {

    private DocumentProcessManager processManager;

    @Mock private TransactionManager transactionManager;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        processManager = new DocumentProcessManager(new DefaultCacheManager().getCache(DocumentProcessManager.DOC_PROCESS_CACHE), transactionManager);

    }

    @Test
    public void testTryLock() {
        // FIXME pahuang java.lang.UnsupportedOperationException: Calling lock() on non-transactional caches is not allowed
        String url = "testing";
        DocumentProcessKey key =
                new DocumentProcessKey(url, LocaleCode.EN, LocaleCode.DE);
        processManager.withLock(key, () -> {
            assertThat(processManager.getTotalLockCount()).isEqualTo(1);
            return Response.ok().build();
        });
        assertThat(processManager.getTotalLockCount()).isEqualTo(0);
    }
}
