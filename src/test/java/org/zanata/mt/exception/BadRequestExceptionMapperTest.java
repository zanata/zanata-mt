package org.zanata.mt.exception;

import org.junit.Test;
import org.zanata.mt.api.dto.APIResponse;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BadRequestExceptionMapperTest {

    @Test
    public void testToResponse() {
        BadRequestException ex = new BadRequestException("testing");
        int badRequestCode = Response.Status.BAD_REQUEST.getStatusCode();
        BadRequestExceptionMapper mapper = new BadRequestExceptionMapper();
        Response response = mapper.toResponse(ex);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(badRequestCode);
        assertThat(response.getEntity()).isInstanceOf(APIResponse.class);
        APIResponse apiResponse = (APIResponse) response.getEntity();
        assertThat(apiResponse.getStatus()).isEqualTo(badRequestCode);
    }
}
