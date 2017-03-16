package org.zanata.mt.api.service;

import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.RequestHeaders;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.zanata.mt.api.dto.APIResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * API entry point for machine translations backend
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Path("/backend")
@RequestHeaders({
        @RequestHeader(name = "X-Auth-User", description = "The authentication user."),
        @RequestHeader(name = "X-Auth-Token", description = "The authentication token.")
})
public interface BackendResource {
    /**
     *
     * @param id
     *      id for machine translations backend
     */
    @GET
    @Path("/attribution")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({"image/png", "application/json"})
    @TypeHint(StreamingOutput.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Attribution found for given id."),
            @ResponseCode(code = 400, condition = "id is missing.", type = @TypeHint(APIResponse.class)),
            @ResponseCode(code = 404, condition = "id not found.", type = @TypeHint(APIResponse.class))
    })
    Response getAttribution(@QueryParam("id") String id);
}
