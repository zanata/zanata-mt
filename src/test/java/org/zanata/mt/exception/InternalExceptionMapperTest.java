package org.zanata.mt.exception;

import org.junit.Test;
import org.zanata.mt.api.dto.APIResponse;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class InternalExceptionMapperTest {

    @Test
    public void testToResponse() {
        Exception ex = new Exception("testing");
        int internalErrorCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        InternalExceptionMapper mapper = new InternalExceptionMapper();
        Response response = mapper.toResponse(ex);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(internalErrorCode);
        assertThat(response.getEntity()).isInstanceOf(APIResponse.class);
        APIResponse apiResponse = (APIResponse) response.getEntity();
        assertThat(apiResponse.getStatus()).isEqualTo(internalErrorCode);
    }
}
