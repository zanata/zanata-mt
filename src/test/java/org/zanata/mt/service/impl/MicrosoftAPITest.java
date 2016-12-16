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
import org.zanata.mt.exception.ZanataMTException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.zanata.mt.service.impl.MicrosoftProvider.AZURE_ID;
import static org.zanata.mt.service.impl.MicrosoftProvider.AZURE_SECRET;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(MicrosoftTranslatorAPI.class)
public class MicrosoftAPITest {
    private MicrosoftTranslatorAPI api;

    @Test
    public void testGetTokenParam() throws UnsupportedEncodingException {
        String id = "id";
        String secret = "secret";
        api = new MicrosoftTranslatorAPI(id, secret);

        String expectedParam = "grant_type=client_credentials&scope=http://api.microsofttranslator.com&client_id=" + id + "&client_secret=" + secret;
        String param = api.getTokenParam();

        assertThat(param).isEqualTo(expectedParam);
    }

    @Test
    public void testGetToken() throws Exception {
        String id = "id";
        String secret = "secret";
        api = new MicrosoftTranslatorAPI(id, secret);
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
