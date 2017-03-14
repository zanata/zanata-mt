package org.zanata.mt.api.dto;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class APIErrorResponseTest {

    @Test
    public void testEmptyConstructor() {
        APIResponse response = new APIResponse();
    }

    @Test
    public void testConstructor() {
        APIResponse response =
                new APIResponse(Response.Status.FORBIDDEN, "error");
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.getTitle()).isEqualTo("error");
    }

    @Test
    public void testConstructor2() {
        Exception e = new Exception("test");
        APIResponse response =
                new APIResponse(Response.Status.FORBIDDEN, e,"error");
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.getTitle()).isEqualTo("error");
        assertThat(response.getDetails()).isEqualTo(e.getMessage());
    }

    @Test
    public void testStatus() {
        APIResponse response = new APIResponse();
        response.setStatus(100);
        assertThat(response.getStatus()).isEqualTo(100);
    }

    @Test
    public void testTitle() {
        APIResponse response = new APIResponse();
        response.setTitle("title");
        assertThat(response.getTitle()).isEqualTo("title");
    }

    @Test
    public void testTimestamp() {
        APIResponse response = new APIResponse();
        response.setTimestamp("18/18/2018");
        assertThat(response.getTimestamp()).isEqualTo("18/18/2018");
    }

    @Test
    public void testDetails() {
        APIResponse response = new APIResponse();
        response.setDetails("details");
        assertThat(response.getDetails()).isEqualTo("details");
    }
}
