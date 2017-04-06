package org.zanata.mt.process;

import org.junit.Before;
import org.junit.Test;
import org.zanata.mt.api.dto.LocaleId;

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
                new DocumentProcessKey(url, LocaleId.EN, LocaleId.DE);
        lock.lock(key);
        assertThat(lock.isLocked(key)).isTrue();
        lock.unlock(key);
        assertThat(lock.isLocked(key)).isFalse();
    }
}
