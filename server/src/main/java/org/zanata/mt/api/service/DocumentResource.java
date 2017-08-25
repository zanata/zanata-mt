package org.zanata.mt.api.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.webcohesion.enunciate.metadata.rs.*;
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
@ResourceLabel("Document")
public interface DocumentResource {

    /**
     * Get request count for a document with given url
     *
     * @param url
     *      URL of the document, mandatory field
     * @param fromLocaleCode
     *      Language code of the document (optional)
     * @param toLocaleCode
     *      Language code of translation for the document (optional)
     * @param dateRange
     *      date range of last updated request document (optional). Format: from..to (yyyy-mm-dd..yyyy-mm-dd)
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/statistics")
    @TypeHint(DocumentStatistics.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Return statistics for given document", type = @TypeHint(DocumentStatistics.class)),
            @ResponseCode(code = 400, condition = "Missing url parameter", type = @TypeHint(APIResponse.class)),
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
     * <b>This is a paid service where cost is based on character count.</b>
     * <br/>
     * <p>'text/plain' - The service will attempt to segment a string that is more than 10,000
     * characters. The string will be returned untranslated if it cannot be segmented.</p>
     *
     * <p>'text/html' - HTML strings must be wrapped in a single HTML node for processing.
     * The service will only process the first HTML node if there are multiple passed in.
     * Example:<br/>
     * `&lt;div&gt;multiple html contents&lt;/div&gt;` - service will translate this whole<br/>
     * `&lt;div&gt;html content1&lt;/div&gt;&lt;div&gt;html content2&lt;/div&gt;` - service will only translate 'html content1' html.<br/>
     * <br/>
     * The service will attempt to segment a html string that is more than 10,000
     * characters by traversing down to a child element that has less than the maximum characters.
     * The string will be returned untranslated if the html element cannot be segmented.
     * <br/>
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
    @TypeHint(DocumentContent.class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Document is translated with given locale", type = @TypeHint(DocumentContent.class)),
            @ResponseCode(code = 400, condition = "Missing toLocaleCode, invalid DocumentContent", type = @TypeHint(APIResponse.class)),
            @ResponseCode(code = 500, condition = "Unexpected error during translation"),
            @ResponseCode(code = 501, condition = "Requested translation provider is not set up")
    })
    Response translate(
            @TypeHint(DocumentContent.class) DocumentContent docContent,
            @QueryParam("toLocaleCode") LocaleCode toLocaleCode);
}
