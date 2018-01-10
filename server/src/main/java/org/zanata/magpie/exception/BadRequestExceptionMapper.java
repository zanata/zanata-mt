package org.zanata.magpie.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.api.dto.APIResponse;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * ExceptionMapper to response to uncaught thrown
 * {@link BadRequestException} in all API requests.
 *
 * If thrown, HTTP response will include {@link APIResponse} entity with
 * {@link Response.Status.BAD_REQUEST} status.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Provider
public class BadRequestExceptionMapper
        implements ExceptionMapper<BadRequestException> {
    private static final Logger LOG =
            LoggerFactory.getLogger(BadRequestExceptionMapper.class);

    @Override
    public Response toResponse(BadRequestException exception) {
        String title = "Bad request error";
        LOG.error(title, exception);
        Response.Status status = Response.Status.BAD_REQUEST;
        APIResponse response = new APIResponse(status, exception, title);
        return Response.status(status).entity(response).build();
    }
}
