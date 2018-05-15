package org.zanata.magpie.backend.ms;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

/**
 * Resteasy client builder for MS service
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
class MicrosoftRestEasyClient {

    protected ResteasyWebTarget getWebTarget(String url) {
        return new ResteasyClientBuilder().build()
                .target(url);
    }

    protected Invocation.Builder getBuilder(String uri, String encoding) {
        return new ResteasyClientBuilder()
                .build()
                .target(uri)
                .request()
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .header("Accept-Charset", encoding);
    }
}
