package org.zanata.mt.backend.ms;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocationBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientRequestHeaders;
import org.junit.Test;
import org.mockito.Mockito;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayReq;
import org.zanata.mt.exception.ZanataMTException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.mt.backend.ms.MicrosoftTranslatorClient.TRANSLATIONS_BASE_URL;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MicrosoftTranslatorClientTest {
    private MicrosoftTranslatorClient api;

    @Test
    public void testGetTokenParam() throws UnsupportedEncodingException {
        String id = "id";
        String secret = "secret";
        api = new MicrosoftTranslatorClient(id, secret);

        String expectedParam = "grant_type=client_credentials&scope=http://api.microsofttranslator.com&client_id=" + id + "&client_secret=" + secret;
        String param = api.getTokenParam();

        assertThat(param).isEqualTo(expectedParam);
    }

    @Test
    public void testGetTokenIfNeeded() {
        String id = "id";
        String secret = "secret";
        api = new MicrosoftTranslatorClient(id, secret);
        String jsonResponse = "{\"expires_in\": \"1\", \"access_token\": \"new\"}";

        Response response = Mockito.mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Response.Status.OK);
        when(response.readEntity(String.class)).thenReturn(jsonResponse);

        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        when(builder.post(any(Entity.class))).thenReturn(response);

        MicrosoftTranslatorClient spyApi = spy(api);
        doReturn(builder).when(spyApi).getBuilder();

        String token = spyApi.getCurrentToken();
        Long tokenExpiry = spyApi.getTokenExpiration();

        spyApi.getTokenIfNeeded();

        assertThat(spyApi.getCurrentToken()).isNotEqualTo(token);
        assertThat(spyApi.getTokenExpiration()).isNotEqualTo(tokenExpiry);
    }

    @Test
    public void testGetToken() throws Exception {
        String id = "id";
        String secret = "secret";
        api = new MicrosoftTranslatorClient(id, secret);
        String expectedToken = "expected token";

        Response response = Mockito.mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Response.Status.OK);
        when(response.readEntity(String.class)).thenReturn(expectedToken);

        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        when(builder.post(any(Entity.class))).thenReturn(response);

        MicrosoftTranslatorClient spyApi = spy(api);
        doReturn(builder).when(spyApi).getBuilder();

        String token = spyApi.getToken();

        verify(builder).post(any(Entity.class));
        verify(response).readEntity(String.class);

        assertThat(token).isEqualTo(expectedToken);
    }

    @Test
    public void testGetTokenException() throws Exception {
        String id = "id";
        String secret = "secret";
        api = new MicrosoftTranslatorClient(id, secret);

        Response response = Mockito.mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        when(builder.post(any(Entity.class))).thenReturn(response);

        MicrosoftTranslatorClient spyApi = spy(api);
        doReturn(builder).when(spyApi).getBuilder();
        assertThatThrownBy(() -> spyApi.getToken())
                .isInstanceOf(ZanataMTException.class);
    }

    @Test
    public void testGetBuilder() {
        String id = "id";
        String secret = "secret";
        api = new MicrosoftTranslatorClient(id, secret);

        Invocation.Builder builder = api.getBuilder();

        ClientRequestHeaders headers = ((ClientInvocationBuilder) builder).getHeaders();
        assertThat(headers.getHeader("Accept-Charset")).isEqualTo("UTF-8");
        assertThat(headers.getHeader("Content-Type")).isEqualTo("application/x-www-form-urlencoded");
    }

    @Test
    public void testGetWebTarget() {
        String id = "id";
        String secret = "secret";
        api = new MicrosoftTranslatorClient(id, secret);

        ResteasyWebTarget webTarget = api.getWebTarget();
        assertThat(webTarget.getUri().toString())
                .isEqualTo(TRANSLATIONS_BASE_URL);
    }

    @Test
    public void testRequestTranslations() {
        String id = "id";
        String secret = "secret";
        api = new MicrosoftTranslatorClient(id, secret);
        String jsonResponse = "{\"expires_in\": \"1\", \"access_token\": \"new\"}";
        String responseXml = "response";

        Response builderResp = Mockito.mock(Response.class);
        when(builderResp.getStatusInfo()).thenReturn(Response.Status.OK);
        when(builderResp.readEntity(String.class)).thenReturn(jsonResponse);

        Response webResp = Mockito.mock(Response.class);
        when(webResp.getStatusInfo()).thenReturn(Response.Status.OK);
        when(webResp.readEntity(String.class)).thenReturn(responseXml);
        ResteasyWebTarget webTarget = Mockito.mock(ResteasyWebTarget.class);
        Invocation.Builder webBuilder = Mockito.mock(Invocation.Builder.class);

        when(webTarget.request(any(String.class))).thenReturn(webBuilder);
        when(webBuilder.header(any(), any())).thenReturn(webBuilder);
        when(webBuilder.post(any())).thenReturn(webResp);

        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        when(builder.post(any(Entity.class))).thenReturn(builderResp);

        MicrosoftTranslatorClient spyApi = spy(api);
        doReturn(builder).when(spyApi).getBuilder();
        doReturn(webTarget).when(spyApi).getWebTarget();

        MSTranslateArrayReq req = new MSTranslateArrayReq();
        String xml = spyApi.requestTranslations(req);

        assertThat(xml).isEqualTo(responseXml);
        verify(webResp).close();
    }

    @Test
    public void testRequestTranslationsException() {
        String id = "id";
        String secret = "secret";
        api = new MicrosoftTranslatorClient(id, secret);
        String jsonResponse = "{\"expires_in\": \"1\", \"access_token\": \"new\"}";

        Response builderResp = Mockito.mock(Response.class);
        when(builderResp.getStatusInfo()).thenReturn(Response.Status.OK);
        when(builderResp.readEntity(String.class)).thenReturn(jsonResponse);

        Response webResp = Mockito.mock(Response.class);
        when(webResp.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);

        ResteasyWebTarget webTarget = Mockito.mock(ResteasyWebTarget.class);
        Invocation.Builder webBuilder = Mockito.mock(Invocation.Builder.class);

        when(webTarget.request(any(String.class))).thenReturn(webBuilder);
        when(webBuilder.header(any(), any())).thenReturn(webBuilder);
        when(webBuilder.post(any())).thenReturn(webResp);

        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        when(builder.post(any(Entity.class))).thenReturn(builderResp);

        MicrosoftTranslatorClient spyApi = spy(api);
        doReturn(builder).when(spyApi).getBuilder();
        doReturn(webTarget).when(spyApi).getWebTarget();

        MSTranslateArrayReq req = new MSTranslateArrayReq();
        assertThatThrownBy(() -> spyApi.requestTranslations(req));
    }
}
