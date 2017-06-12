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
    public void testEmptyConstructor() {
        ZanataMTStartup app = new ZanataMTStartup();
    }

    @Test
    public void testEmptyCredentials() {
        ZanataMTStartup app = new ZanataMTStartup(null, null);
        assertThatThrownBy(() -> app.onStartUp(null))
                .isInstanceOf(ZanataMTException.class);
    }

    @Test
    public void testOnStartup() {
        ZanataMTStartup app = new ZanataMTStartup("id", "id");
        app.onStartUp(null);
    }

    @Test
    public void testDevModeEnabledByDefault() {
        ZanataMTStartup app = new ZanataMTStartup("id", "id");
        app.onStartUp(null);
        assertThat(app.isDevMode()).isTrue();
    }
}
