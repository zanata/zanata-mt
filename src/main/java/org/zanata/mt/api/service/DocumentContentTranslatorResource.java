package org.zanata.mt.api.service;

import javax.ws.rs.Consumes;
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
import org.zanata.mt.api.dto.LocaleId;

/**
 * API entry point for {@link DocumentContent}  translation
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Path("/document")
@RequestHeaders({
        @RequestHeader(name = "X-Auth-User", description = "The authentication user."),
        @RequestHeader(name = "X-Auth-Token", description = "The authentication token.")
})
public interface DocumentContentTranslatorResource {

    // Max length per request for MS
    public static final int MAX_LENGTH = 6000;

    // Max length before logging warning
    public static final int MAX_LENGTH_WARN = 3000;

    /**
     * Maximum accepted characters in a request is 6000: {@link #MAX_LENGTH}
     *
     * @param targetLang
     *      target language to translate
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/translate")
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Document is translated with given locale.", type = @TypeHint(DocumentContent.class)),
            @ResponseCode(code = 400, condition = "Missing targetLang, invalid DocumentContent, exceed 6000 characters in request", type = @TypeHint(APIResponse.class)),
            @ResponseCode(code = 500, condition = "Unexpected error during translation.", type = @TypeHint(APIResponse.class))
    })
    Response translate(
            @TypeHint(DocumentContent.class) DocumentContent docContent,
            @QueryParam("targetLang") LocaleId targetLang);
}
