package org.zanata.mt.process;

import org.junit.Before;
import org.junit.Test;
import org.zanata.mt.api.dto.LocaleCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocumentProcessManagerTest {

    private DocumentProcessManager lock;

    @Before
    public void init() {
        lock = new DocumentProcessManager();
    }

    @Test
    public void testTryLock() {
        String url = "testing";
        DocumentProcessKey key =
                new DocumentProcessKey(url, LocaleCode.EN, LocaleCode.DE);
        lock.lock(key);
        assertThat(lock.isLocked(key)).isTrue();
        assertThat(lock.getLock(key).getHoldCount()).isEqualTo(1);
        lock.unlock(key);
        assertThat(lock.isLocked(key)).isFalse();
        assertThat(lock.getLock(key).getHoldCount()).isEqualTo(0);
    }
}
