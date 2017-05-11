package org.zanata.mt.api.service;

import com.webcohesion.enunciate.metadata.rs.*;
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
@ResourceLabel("Documents")
public interface LanguagesResource {

    /**
     * @return all supported languages
     *
     * Supported locales:
     * <p><ul>
     * <li>en-us (English)
     * <li>de (German)
     * <li>es (Spanish)
     * <li>fr (French)
     * <li>it (Italian)
     * <li>ja (Japanese)
     * <li>ko (Korean)
     * <li>pt (Portuguese)
     * <li>ru (Russian)
     * <li>zh-hans (Chinese (Simplified))
     * <li>zh-hant (Chinese (Traditional))
     * </ul></p>
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(Locale[].class)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "List of supported languages", type = @TypeHint(Locale[].class)),
            @ResponseCode(code = 500, condition = "Unexpected error")
    })
    Response getSupportedLanguages();
}
