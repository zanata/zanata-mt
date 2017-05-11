package org.zanata.mt.api.service;

import com.webcohesion.enunciate.metadata.rs.*;
import org.zanata.mt.api.dto.APIResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * API entry point for documents
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Path("/documents")
@RequestHeaders({
        @RequestHeader(name = "X-Auth-User", description = "The authentication user."),
        @RequestHeader(name = "X-Auth-Token", description = "The authentication token.")
})
@ResourceLabel("Documents")
public interface DocumentsResource {

    /**
     * Return a list of document urls.
     *
     * This can be used with {@link DocumentResource#getStatistics}
     * to retrieve detailed getStatistics of a document
     *
     * @param dateRange
     *      date range of last updated request document (optional). Format: from..to (yyyy-mm-dd..yyyy-mm-dd)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "List of document urls", type = @TypeHint(String[].class)),
            @ResponseCode(code = 500, condition = "Unexpected error")
    })
    Response getDocumentUrls(@QueryParam("dateRange") String dateRange);
}
