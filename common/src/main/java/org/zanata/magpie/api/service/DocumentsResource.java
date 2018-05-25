package org.zanata.magpie.api.service;

import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.RequestHeaders;
import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.magpie.api.APIConstant;

/**
 * API entry point for documents
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Path("/documents")
@RequestHeaders({
        @RequestHeader(name = APIConstant.HEADER_USERNAME, description = "The authentication user."),
        @RequestHeader(name = APIConstant.HEADER_API_KEY, description = "The authentication token.")
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
    @TypeHint(String[].class)
    Response getDocumentUrls(@QueryParam("dateRange") String dateRange);
}
