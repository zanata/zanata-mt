package org.zanata.mt.service.impl;

import org.junit.Test;
import org.zanata.mt.exception.ZanataMTException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MicrosoftBackendIDTest {
    private MicrosoftProvider provider = null;

    @Test
    public void testVerifyCredentialsInvalid() {
        provider = new MicrosoftProvider(null, null);
        assertThatThrownBy(() -> provider.init())
            .isInstanceOf(ZanataMTException.class);
    }

    @Test
    public void testVerifyCredentials() {
        provider = new MicrosoftProvider("id", "secret");
        provider.init();
    }

    @Test
    public void testClientId() {
        String id = "client_id";
        provider = new MicrosoftProvider(id, null);
        assertThat(provider.getClientId()).isEqualTo(id);
    }

    @Test
    public void testClientSecret() {
        String secret = "client_secret";
        provider = new MicrosoftProvider(null, secret);
        assertThat(provider.getClientSecret()).isEqualTo(secret);
    }
}
