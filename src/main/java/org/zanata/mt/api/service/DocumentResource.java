package org.zanata.mt.api.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.RequestHeaders;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.zanata.mt.api.dto.APIResponse;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.DocumentStatistics;
import org.zanata.mt.api.dto.LocaleCode;

/**
 * API entry point for Document services.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Path("/document")
@RequestHeaders({
        @RequestHeader(name = "X-Auth-User", description = "The authentication user."),
        @RequestHeader(name = "X-Auth-Token", description = "The authentication token.")
})
public interface DocumentResource {

    // Max length per request
    int MAX_LENGTH = 10000;

    /**
     * Get request count for a document with given url
     *
     * @param url
     *      URL of the document, mandatory field
     * @param fromLocaleCode
     *      Language code of the document, optional
     * @param toLocaleCode
     *      Language code of translation for the document, optional
     * @param dateRange
     *      date range of last updated request document(Optional). Format: from..to (yyyy-mm-dd..yyyy-mm-dd)
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/statistics")
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Return statistics for given document", type = @TypeHint(DocumentStatistics.class)),
            @ResponseCode(code = 400, condition = "Missing url", type = @TypeHint(APIResponse.class)),
            @ResponseCode(code = 500, condition = "Unexpected error")
    })
    Response getStatistics(@QueryParam("url") String url,
            @QueryParam("fromLocaleCode") LocaleCode fromLocaleCode,
            @QueryParam("toLocaleCode") LocaleCode toLocaleCode,
            @QueryParam("dateRange") String dateRange);

    /**
     * Perform machine translation on {@link DocumentContent#contents} to given
     * locale code.<br/>
     *
     * <b>This is a paid service which cost is based on character count.</b><br/>
     *
     * <p>'text/plain' - Service will try to segment string that is more than 10,000
     * characters. String will be ignore if it is unable to be segmented.</p>
     *
     * <p>'text/html' - HTML string must be wrapped in a single HTML node for processing.
     * Service will only process the first HTML node if there are multiple passed in.
     * Example:<br/>
     * `<div>multiple html contents</div>` - service will translate this whole<br/>
     * `<div>html content1</div><div>html content2</div>` - service will only translate 'html content1' html.<br/>
     *
     * Service will try to split string that is more than 10,000
     * characters by running down to child element that has less than maximum chars.
     * String will be ignored if html element cannot be broken down further.
     *
     * The content in parent element of the translated child element will not be
     * translated.</p>
     *
     * See {@link LanguagesResource#getSupportedLanguages()} for supported locales.
     *
     * @param docContent
     *      Content to be translated
     * @param toLocaleCode
     *      Language code to translate to
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/translate")
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Document is translated with given locale", type = @TypeHint(DocumentContent.class)),
            @ResponseCode(code = 400, condition = "Missing toLocaleCode, invalid DocumentContent", type = @TypeHint(APIResponse.class)),
            @ResponseCode(code = 500, condition = "Unexpected error during translation")
    })
    Response translate(
            @TypeHint(DocumentContent.class) DocumentContent docContent,
            @QueryParam("toLocaleCode") LocaleCode toLocaleCode);
}
