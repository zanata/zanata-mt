package org.zanata.mt.service;

import org.junit.Test;
import org.zanata.mt.exception.ZanataMTException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ZanataMTStartupTest {

    @Test
    public void testEmptyCredentials() {
        ConfigurationService config = new ConfigurationService();
        ZanataMTStartup app = new ZanataMTStartup(config);
        assertThatThrownBy(() -> app.onStartUp(null))
                .isInstanceOf(ZanataMTException.class);
    }

    @Test
    public void testOnStartup() {
        ConfigurationService config = new ConfigurationService("id", "key");
        ZanataMTStartup app = new ZanataMTStartup(config);
        app.onStartUp(null);
    }
}
