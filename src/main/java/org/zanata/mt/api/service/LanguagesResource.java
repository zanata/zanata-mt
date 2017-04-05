package org.zanata.mt.api.service;

import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.RequestHeaders;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.zanata.mt.api.dto.APIResponse;
import org.zanata.mt.api.dto.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * API entry point for languages
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Path("/languages")
@RequestHeaders({
        @RequestHeader(name = "X-Auth-User", description = "The authentication user."),
        @RequestHeader(name = "X-Auth-Token", description = "The authentication token.")
})
public interface LanguagesResource {

    /**
     * @return all supported languages
     *
     * Supported locales:
     *
     * - en-us (English)
     * - de (German)
     * - es (Spanish)
     * - fr (French)
     * - it (Italian)
     * - ja (Japanese)
     * - ko (Korean)
     * - pt (Portuguese)
     * - ru (Russian)
     * - zh-hans (Chinese (Simplified))
     * - zh-hant (Chinese (Traditional))
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "List of supported languages", type = @TypeHint(Locale[].class)),
            @ResponseCode(code = 500, condition = "Unexpected error.", type = @TypeHint(APIResponse.class))
    })
    Response getSupportedLanguages();
}
