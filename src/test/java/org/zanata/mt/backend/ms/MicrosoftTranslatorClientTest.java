package org.zanata.mt.backend.ms;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayReq;
import org.zanata.mt.exception.ZanataMTException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MicrosoftTranslatorClientTest {
    private MicrosoftTranslatorClient api;
    private MicrosoftRestEasyClient restClient;
    private String id = "id";
    private String secret = "secret";

    @Before
    public void setup() {
        restClient = Mockito.mock(MicrosoftRestEasyClient.class);
        api = new MicrosoftTranslatorClient(id, secret, restClient);
    }

    @Test
    public void testGetTokenParam() throws UnsupportedEncodingException {
        String expectedParam = "grant_type=client_credentials&scope=http://api.microsofttranslator.com&client_id=" + id + "&client_secret=" + secret;
        String param = api.getTokenParam();

        assertThat(param).isEqualTo(expectedParam);
    }

    @Test
    public void testGetTokenIfNeeded() {
        String jsonResponse = "{\"expires_in\": \"1\", \"access_token\": \"new\"}";

        Response response = Mockito.mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Response.Status.OK);
        when(response.readEntity(String.class)).thenReturn(jsonResponse);

        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        when(builder.post(any(Entity.class))).thenReturn(response);
        when(restClient.getBuilder(any(), any())).thenReturn(builder);

        String token = api.getCurrentToken();
        Long tokenExpiry = api.getTokenExpiration();

        api.getTokenIfNeeded();

        assertThat(api.getCurrentToken()).isNotEqualTo(token);
        assertThat(api.getTokenExpiration()).isNotEqualTo(tokenExpiry);
    }

    @Test
    public void testGetToken() throws Exception {
        String expectedToken = "expected token";

        Response response = Mockito.mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Response.Status.OK);
        when(response.readEntity(String.class)).thenReturn(expectedToken);

        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        when(builder.post(any(Entity.class))).thenReturn(response);
        when(restClient.getBuilder(any(), any())).thenReturn(builder);

        String token = api.getToken();

        verify(builder).post(any(Entity.class));
        verify(response).readEntity(String.class);

        assertThat(token).isEqualTo(expectedToken);
    }

    @Test
    public void testGetTokenException() throws Exception {
        Response response = Mockito.mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        when(builder.post(any(Entity.class))).thenReturn(response);
        when(restClient.getBuilder(any(), any())).thenReturn(builder);

        assertThatThrownBy(() -> api.getToken())
                .isInstanceOf(ZanataMTException.class);
    }

    @Test
    public void testRequestTranslations() {
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

        when(restClient.getBuilder(any(), any())).thenReturn(builder);
        when(restClient.getWebTarget(any())).thenReturn(webTarget);

        MSTranslateArrayReq req = new MSTranslateArrayReq();
        String xml = api.requestTranslations(req);

        assertThat(xml).isEqualTo(responseXml);
        verify(webResp).close();
    }

    @Test
    public void testRequestTranslationsException() {
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

        when(restClient.getBuilder(any(), any())).thenReturn(builder);
        when(restClient.getWebTarget(any())).thenReturn(webTarget);

        MSTranslateArrayReq req = new MSTranslateArrayReq();
        assertThatThrownBy(() -> api.requestTranslations(req));
    }
}
