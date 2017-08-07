package org.zanata.mt.api.service;

import java.io.File;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.mt.api.service.impl.BackendResourceImpl;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BackendResourceTest {

    private BackendResource backendResource;
    @Mock private InputStream inputStream;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        backendResource = new BackendResourceImpl() {
            @Override
            protected InputStream getResourceAsStream(String imageResource) {
                return inputStream;
            }
        };
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
                .contains(new File(BackendResource.MS_ATTRIBUTION_IMAGE).getName());
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
                .contains(new File(BackendResource.DEV_ATTRIBUTION_IMAGE).getName());
    }

    @Test
    public void testGetAttributionInvalidId() {
        String id = "blah";
        Response response = backendResource.getAttribution(id);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void returnNotFoundIfGetAttributionCanNotFindImage() {
        backendResource = new BackendResourceImpl() {
            @Override
            protected InputStream getResourceAsStream(String imageResource) {
                return null;
            }
        };
        Response response = backendResource.getAttribution("google");
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
