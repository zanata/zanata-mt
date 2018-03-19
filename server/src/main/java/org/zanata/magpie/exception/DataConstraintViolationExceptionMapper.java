package org.zanata.magpie.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.api.dto.APIResponse;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Provider
public class DataConstraintViolationExceptionMapper
        implements ExceptionMapper<DataConstraintViolationException> {
    private static final Logger LOG =
            LoggerFactory.getLogger(DataConstraintViolationExceptionMapper.class);

    @Override
    public Response toResponse(DataConstraintViolationException exception) {
        String title = "constraint violation exception";
        LOG.error(title, exception);
        Response.Status status = Response.Status.CONFLICT;
        APIResponse response = new APIResponse(status, exception, title);
        return Response.status(status).entity(response).build();
    }
}
