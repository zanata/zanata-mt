package org.magpie.mt.exception;

import org.junit.Test;
import org.magpie.mt.api.dto.APIResponse;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MTExceptionMapperTest {

    @Test
    public void testToResponse() {
        ZanataMTException ex = new ZanataMTException("testing");
        int internalErrorCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        MTExceptionMapper mapper = new MTExceptionMapper();
        Response response = mapper.toResponse(ex);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(internalErrorCode);
        assertThat(response.getEntity()).isInstanceOf(APIResponse.class);
        APIResponse apiResponse = (APIResponse) response.getEntity();
        assertThat(apiResponse.getStatus()).isEqualTo(internalErrorCode);
    }
}
