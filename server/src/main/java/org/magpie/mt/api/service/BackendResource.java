package org.magpie.mt.api.service;

import com.webcohesion.enunciate.metadata.rs.*;
import org.magpie.mt.api.dto.APIResponse;

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
@ResourceLabel("Backend")
public interface BackendResource {
    String MS_ATTRIBUTION_IMAGE =
            "/images/MS_attribution.png";
    String GOOGLE_ATTRIBUTION_IMAGE =
                    "/images/google_attribution.png";
    String DEV_ATTRIBUTION_IMAGE =
                            "/images/logo-256.png";

    /**
     * Retrieve backend attribution (image) based on given id
     *
     * @param id
     *      ID for machine translations backend
     */
    @GET
    @Path("/attribution")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({"image/png", "application/json"})
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Attribution found for given id.", type = @TypeHint(StreamingOutput.class)),
            @ResponseCode(code = 400, condition = "id parameter is missing.", type = @TypeHint(APIResponse.class)),
            @ResponseCode(code = 404, condition = "id not recognised.", type = @TypeHint(APIResponse.class)),
            @ResponseCode(code = 500, condition = "Unexpected error.")
    })
    Response getAttribution(@QueryParam("id") String id);


    /**
     * Available machine translation providers.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Available machine translation providers", type = @TypeHint(String[].class)),
            @ResponseCode(code = 500, condition = "Unexpected error.")
    })
    Response getAvailableBackends();
}
