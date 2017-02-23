package org.zanata.mt.backend.ms;

import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocationBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientRequestHeaders;
import org.junit.Test;

import javax.ws.rs.client.Invocation;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MicrosoftRestEasyClientTest {

    @Test
    public void testGetBuilder() {
        MicrosoftRestEasyClient client = new MicrosoftRestEasyClient();

        Invocation.Builder builder = client.getBuilder("http://localhost", "UTF-8");

        ClientRequestHeaders headers = ((ClientInvocationBuilder) builder).getHeaders();
        assertThat(headers.getHeader("Accept-Charset")).isEqualTo("UTF-8");
        assertThat(headers.getHeader("Content-Type")).isEqualTo("application/x-www-form-urlencoded");
    }

    @Test
    public void testGetWebTarget() {
        MicrosoftRestEasyClient client = new MicrosoftRestEasyClient();

        ResteasyWebTarget webTarget = client.getWebTarget("http://url");
        assertThat(webTarget.getUri().toString())
                .isEqualTo("http://url");
    }

}
