package org.zanata.magpie.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.api.dto.APIResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * ExceptionMapper to response to uncaught thrown
 * {@link MTException} in all API requests.
 *
 * If thrown, HTTP response will include {@link APIResponse} entity with
 * {@link Response.Status.INTERNAL_SERVER_ERROR} status.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Provider
public class MTExceptionMapper
        implements ExceptionMapper<MTException> {
    private static final Logger LOG =
            LoggerFactory.getLogger(MTExceptionMapper.class);

    @Override
    public Response toResponse(MTException exception) {
        String title = "Internal server error";
        LOG.error(title, exception);
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        APIResponse response = new APIResponse(status, exception, title);
        return Response.status(status).entity(response).build();
    }
}
