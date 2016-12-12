package org.zanata.mt.service;

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.zanata.mt.exception.TranslationEngineException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.zanata.mt.service.MicrosoftTranslatorAPI.AZURE_ID;
import static org.zanata.mt.service.MicrosoftTranslatorAPI.AZURE_SECRET;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MicrosoftAPITest {

    @Test(expected = TranslationEngineException.class)
    public void testVerifyCredentialsInvalid() throws TranslationEngineException {
        MicrosoftTranslatorAPI.verifyCredentials();
    }

    @Test
    public void testVerifyCredentials() throws TranslationEngineException {
        System.setProperty(AZURE_ID, "id");
        System.setProperty(AZURE_SECRET, "secret");
        MicrosoftTranslatorAPI.verifyCredentials();
    }

    @Test
    public void testClientId() {
        String id = "client_id";
        System.setProperty(AZURE_ID, id);
        assertThat(MicrosoftTranslatorAPI.getClientId()).isEqualTo(id);
    }

    @Test
    public void testClientSecret() {
        String secret = "client_secret";
        System.setProperty(AZURE_SECRET, secret);
        assertThat(MicrosoftTranslatorAPI.getSecret()).isEqualTo(secret);
    }

    @Test
    public void testGetTokenParam() throws UnsupportedEncodingException {
        String id = "id";
        String secret = "secret";
        System.setProperty(AZURE_ID, id);
        System.setProperty(AZURE_SECRET, secret);

        String expectedParam = "grant_type=client_credentials&scope=http://api.microsofttranslator.com&client_id=" + id + "&client_secret=" + secret;
        String param = MicrosoftTranslatorAPI.getTokenParam();

        assertThat(param).isEqualTo(expectedParam);
    }
}
