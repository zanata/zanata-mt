package org.zanata.magpie.api.service;

import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.zanata.magpie.api.dto.ApplicationInfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * API for information of the application
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Path("/info")
@ResourceLabel("Info")
public interface InfoResource {

    /**
     * @return General information of the application
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(ApplicationInfo.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Application info", type = @TypeHint(ApplicationInfo.class)),
            @ResponseCode(code = 500, condition = "Unexpected error")
    })
    Response getInfo();
}
