package org.zanata.mt.service.impl;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zanata.mt.exception.TranslationProviderException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.zanata.mt.service.impl.MicrosoftTranslatorAPI.AZURE_ID;
import static org.zanata.mt.service.impl.MicrosoftTranslatorAPI.AZURE_SECRET;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(MicrosoftTranslatorAPI.class)
public class MicrosoftAPITest {
    private MicrosoftTranslatorAPI api;
    @Before
    public void init() {
        System.clearProperty(AZURE_ID);
        System.clearProperty(AZURE_SECRET);
        api = new MicrosoftTranslatorAPI();
    }

    @Test(expected = TranslationProviderException.class)
    public void testVerifyCredentialsInvalid() throws
        TranslationProviderException {
        api.verifyCredentials();
    }

    @Test
    public void testVerifyCredentials() throws TranslationProviderException {
        System.setProperty(AZURE_ID, "id");
        System.setProperty(AZURE_SECRET, "secret");
        api.verifyCredentials();
    }

    @Test
    public void testClientId() {
        String id = "client_id";
        System.setProperty(AZURE_ID, id);
        assertThat(api.getClientId()).isEqualTo(id);
    }

    @Test
    public void testClientSecret() {
        String secret = "client_secret";
        System.setProperty(AZURE_SECRET, secret);
        assertThat(api.getSecret()).isEqualTo(secret);
    }

    @Test
    public void testGetTokenParam() throws UnsupportedEncodingException {
        String id = "id";
        String secret = "secret";
        System.setProperty(AZURE_ID, id);
        System.setProperty(AZURE_SECRET, secret);

        String expectedParam = "grant_type=client_credentials&scope=http://api.microsofttranslator.com&client_id=" + id + "&client_secret=" + secret;
        String param = api.getTokenParam();

        assertThat(param).isEqualTo(expectedParam);
    }

    @Test
    public void testGetToken() throws Exception {
        String id = "id";
        String secret = "secret";
        System.setProperty(AZURE_ID, id);
        System.setProperty(AZURE_SECRET, secret);
        String expectedToken = "expected token";

        Response response = PowerMockito.mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Response.Status.OK);
        when(response.readEntity(String.class)).thenReturn(expectedToken);

        Invocation.Builder builder = PowerMockito.mock(Invocation.Builder.class);
        when(builder.post(any(Entity.class))).thenReturn(response);

        MicrosoftTranslatorAPI spyApi = spy(api);
        doReturn(builder).when(spyApi).getBuilder();

        String token = spyApi.getToken();

        verify(builder).post(any(Entity.class));
        verify(response).readEntity(String.class);

        assertThat(token).isEqualTo(expectedToken);
    }
}
