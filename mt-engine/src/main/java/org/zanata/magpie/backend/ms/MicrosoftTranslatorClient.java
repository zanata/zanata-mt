package org.zanata.magpie.backend.ms;

import java.util.List;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.codec.CharEncoding;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.backend.ms.internal.dto.MSString;
import org.zanata.magpie.backend.ms.internal.dto.MSTranslateArrayReq;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.util.DTOUtil;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * Client for Microsoft Translator service using RestEasy.
 * Uses the HTTP Interface V2 - see: https://msdn.microsoft.com/en-us/library/ff512422.aspx
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
class MicrosoftTranslatorClient {

    private static final Logger LOG =
        LoggerFactory.getLogger(MicrosoftTranslatorClient.class);

    // properties
    // https://api.cognitive.microsofttranslator.com/translate?api-version=3.0
    private static final String TRANSLATIONS_BASE_URL = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0";

    private static final String DATA_MARKET_ACCESS_URI = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";
    private static final String OCP_APIM_SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";
    private static final String ENCODING = CharEncoding.UTF_8;

    // Cache token for 5 minutes
    private static final long TOKEN_CACHE_EXPIRATION = 5 * 60 * 1000;

    private long tokenExpiration = 0;
    private String token;

    private final String clientSubscriptionKey;

    private final MicrosoftRestEasyClient restClient;
    private final DTOUtil dtoUtil;

    protected MicrosoftTranslatorClient(String clientSubscriptionKey,
            MicrosoftRestEasyClient restClient, DTOUtil dtoUtil) {
        this.clientSubscriptionKey = clientSubscriptionKey;
        this.restClient = restClient;
        this.dtoUtil = dtoUtil;
    }

    /**
     * Get access token from MS API if token is expired.
     * 10 minutes by default
     */
    protected void getTokenIfNeeded() {
        if (System.currentTimeMillis() > tokenExpiration) {
            String tokenKey = getToken();
            tokenExpiration = System.currentTimeMillis() +
                    TOKEN_CACHE_EXPIRATION;
            token = "Bearer " + tokenKey;
            if (LOG.isDebugEnabled()) {
                LOG.debug("New token:{}", token);
            }
        }
    }

    /**
     * Return raw response from Microsoft API
     */
    protected String requestTranslations(MSTranslateArrayReq req,
            String fromLocale, String toLocale,
            Optional<String> category, MediaType mediaType)
            throws MTException {
        getTokenIfNeeded();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Source sending:{}", dtoUtil.toJSON(req));
        }
        ResteasyWebTarget webTarget =
                restClient.getWebTarget(TRANSLATIONS_BASE_URL)
                        .queryParam("from", fromLocale)
                        .queryParam("to", toLocale)
                        .queryParam("textType", mediaType.equals(MediaType.TEXT_HTML_TYPE) ? "html" : "plain")
                ;
        category.ifPresent(cat -> webTarget.queryParam("category", cat));

        List<MSString> texts = req.getTexts();

        String entity = dtoUtil.toJSON(texts);

        Response response = webTarget.request(MediaType.APPLICATION_JSON)

                .header("Content-Type",
                        MediaType.APPLICATION_JSON + "; charset=" + ENCODING)
                .header("Authorization", token)
//                .header("Content-Length", entity.length())

                .post(Entity.json(entity));

        if (response.getStatusInfo() != Response.Status.OK) {
            throw new MTException(
                    "Error from Microsoft Translator API:"
                            + response.getStatusInfo().getReasonPhrase());
        }
        String json = response.readEntity(String.class);
        response.close();
        LOG.debug("Translation from Microsoft Engine:{}", json);
        return json;
    }

    /**
     * Get access token from MS API
     */
    protected String getToken() throws MTException {
        Response response = null;
        try {
            LOG.debug("Getting token for Microsoft Engine");

            Invocation.Builder builder =
                    restClient.getBuilder(DATA_MARKET_ACCESS_URI, ENCODING);
            builder.header(OCP_APIM_SUBSCRIPTION_KEY_HEADER,
                    clientSubscriptionKey);

            response = builder.build("POST").invoke();

            if (response.getStatusInfo() != Response.Status.OK) {
                throw new MTException(
                        "Error getting token:" + response.getStatusInfo().getReasonPhrase());
            }
            return response.readEntity(String.class);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @VisibleForTesting
    protected String getCurrentToken() {
        return token;
    }

    @VisibleForTesting
    protected Long getTokenExpiration() {
        return tokenExpiration;
    }
}
