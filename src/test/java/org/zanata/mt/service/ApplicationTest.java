package org.zanata.mt.service;

import org.junit.Test;
import org.zanata.mt.exception.ZanataMTException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ApplicationTest {

    @Test
    public void testEmptyCredentials() {
        Application app = new Application(null, null);
        assertThatThrownBy(() -> app.onStartUp(null))
                .isInstanceOf(ZanataMTException.class);
    }

    @Test
    public void testOnStartup() {
        Application app = new Application("id", "id");
        app.onStartUp(null);
    }
}
