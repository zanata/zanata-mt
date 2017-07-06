package org.zanata.mt.api.service;

import org.junit.Before;
import org.junit.Test;
import org.zanata.mt.api.service.impl.BackendResourceImpl;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BackendResourceTest {

    private BackendResource backendResource;

    @Before
    public void setup() {
        backendResource = new BackendResourceImpl();
    }

    @Test
    public void testGetAttributionNullId() {
        Response response = backendResource.getAttribution(null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAttributionMSLowerCase() {
        String id = "ms";
        Response response = backendResource.getAttribution(id);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getHeaders()).isNotEmpty()
                .containsKeys("Content-Disposition");
        assertThat(
                (String) response.getHeaders().getFirst("Content-Disposition"))
                .contains(id);
    }

    @Test
    public void testGetAttributionDEVLowerCase() {
        String id = "dev";
        Response response = backendResource.getAttribution(id);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getHeaders()).isNotEmpty()
                .containsKeys("Content-Disposition");
        assertThat(
                (String) response.getHeaders().getFirst("Content-Disposition"))
                .contains(id);
    }

    @Test
    public void testGetAttributionInvalidId() {
        String id = "google";
        Response response = backendResource.getAttribution(id);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
