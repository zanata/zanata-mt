package org.zanata.mt.backend.ms;

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
    private String subscriptionKey = "key";

    private Invocation invocation;
    private Invocation.Builder builder;

    @Before
    public void setup() {
        restClient = Mockito.mock(MicrosoftRestEasyClient.class);
        api = new MicrosoftTranslatorClient(subscriptionKey, restClient);

        builder = Mockito.mock(Invocation.Builder.class);
        invocation = Mockito.mock(Invocation.class);

        when(builder.build("POST")).thenReturn(invocation);
        when(restClient.getBuilder(any(), any())).thenReturn(builder);
    }

    @Test
    public void testGetTokenIfNeeded() {
        String responseKey = "randomKeyThatReturnsFromMS";

        Response response = Mockito.mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Response.Status.OK);
        when(response.readEntity(String.class)).thenReturn(responseKey);
        when(invocation.invoke()).thenReturn(response);

        String token = api.getCurrentToken();
        Long tokenExpiry = api.getTokenExpiration();

        api.getTokenIfNeeded();

        assertThat(api.getCurrentToken()).isNotEqualTo(token);
        assertThat(api.getTokenExpiration()).isNotEqualTo(tokenExpiry);
    }

    @Test
    public void testGetToken() throws Exception {
        String responseKey = "randomKeyThatReturnsFromMS";

        Response response = Mockito.mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Response.Status.OK);
        when(response.readEntity(String.class)).thenReturn(responseKey);
        when(invocation.invoke()).thenReturn(response);

        String token = api.getToken();

        verify(builder).build("POST");
        verify(response).readEntity(String.class);

        assertThat(token).isEqualTo(responseKey);
    }

    @Test
    public void testGetTokenException() throws Exception {
        Response response = Mockito.mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
        when(invocation.invoke()).thenReturn(response);

        assertThatThrownBy(() -> api.getToken())
                .isInstanceOf(ZanataMTException.class);
    }

    @Test
    public void testRequestTranslations() {
        String responseKey = "randomKeyThatReturnsFromMS";
        String responseXml = "response";

        Response builderResp = Mockito.mock(Response.class);
        when(builderResp.getStatusInfo()).thenReturn(Response.Status.OK);
        when(builderResp.readEntity(String.class)).thenReturn(responseKey);

        Response webResp = Mockito.mock(Response.class);
        when(webResp.getStatusInfo()).thenReturn(Response.Status.OK);
        when(webResp.readEntity(String.class)).thenReturn(responseXml);
        ResteasyWebTarget webTarget = Mockito.mock(ResteasyWebTarget.class);
        Invocation.Builder webBuilder = Mockito.mock(Invocation.Builder.class);

        when(webTarget.request(any(String.class))).thenReturn(webBuilder);
        when(webBuilder.header(any(), any())).thenReturn(webBuilder);
        when(webBuilder.post(any())).thenReturn(webResp);

        when(invocation.invoke()).thenReturn(builderResp);
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

        when(invocation.invoke()).thenReturn(builderResp);
        when(restClient.getWebTarget(any())).thenReturn(webTarget);

        MSTranslateArrayReq req = new MSTranslateArrayReq();
        assertThatThrownBy(() -> api.requestTranslations(req));
    }
}
