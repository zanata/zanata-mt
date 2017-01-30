package org.zanata.mt.backend.ms;

import org.junit.Test;
import org.zanata.mt.backend.ms.MicrosoftTranslatorBackend;
import org.zanata.mt.exception.ZanataMTException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MicrosoftTranslatorBackendTest {
    private MicrosoftTranslatorBackend msBackend = null;

    @Test
    public void testVerifyCredentialsInvalid() {
        msBackend = new MicrosoftTranslatorBackend(null, null);
        assertThatThrownBy(() -> msBackend.onInit(null))
            .isInstanceOf(ZanataMTException.class);
    }

    @Test
    public void testVerifyCredentials() {
        msBackend = new MicrosoftTranslatorBackend("id", "secret");
        msBackend.onInit(null);
    }

    @Test
    public void testClientId() {
        String id = "client_id";
        msBackend = new MicrosoftTranslatorBackend(id, null);
        assertThat(msBackend.getClientId()).isEqualTo(id);
    }

    @Test
    public void testClientSecret() {
        String secret = "client_secret";
        msBackend = new MicrosoftTranslatorBackend(null, secret);
        assertThat(msBackend.getClientSecret()).isEqualTo(secret);
    }

    @Test
    public void testAttributionSmall() {
        msBackend = new MicrosoftTranslatorBackend(null, null);
        String attribution = msBackend.getAttributionSmall();
        assertThat(attribution).contains(msBackend.ATTRIBUTION_REF);
    }

    @Test
    public void testAttributionMedium() {
        msBackend = new MicrosoftTranslatorBackend(null, null);
        String attribution = msBackend.getAttributionMedium();
        assertThat(attribution).contains(msBackend.ATTRIBUTION_REF);
    }

    @Test
    public void testAttribution() {
        msBackend = new MicrosoftTranslatorBackend(null, null);
        String attribution = msBackend.getAttribution();
        assertThat(attribution).contains(msBackend.ATTRIBUTION_REF);
    }
}
