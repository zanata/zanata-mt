package org.zanata.mt.service;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.dto.Microsoft.MSTranslateArrayReq;
import org.zanata.mt.exception.TranslationEngineException;
import org.zanata.mt.util.DTOUtil;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 *
 * API for Microsoft Translator service using RestEasy.
 * Uses the HTTP Interface V2 - see: https://msdn.microsoft.com/en-us/library/ff512422.aspx
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MicrosoftTranslatorAPI {

    private static final Logger log =
        LoggerFactory.getLogger(MicrosoftTranslatorAPI.class);

    public static final String TRANSLATIONS_BASE_URL = "http://api.microsofttranslator.com/V2/Http.svc/TranslateArray";
    public static final String DATA_MARKET_ACCESS_URI = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";

    public static final String AZURE_ID = "AZURE_ID";
    public static final String AZURE_SECRET = "AZURE_SECRET";

    public static final String ENCODING = CharEncoding.UTF_8;
    public static final String MEDIA_TYPE = MediaType.TEXT_HTML;

    public static final String OPTIONS_NAMESPACE = "http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2";

    private static final String templateTokenParam =
        "grant_type=client_credentials&scope=http://api.microsofttranslator.com&client_id=${clientId}&client_secret=${clientSecret}";

    // The access token is valid for 10 minutes
    private static long tokenExpiration = 0;
    private static String token;

    /**
     * Get access token from MS API if token has expired.
     * 10 minutes by default
     *
     * @throws Exception
     */
    protected static void getTokenIfNeeded() throws Exception {
        if (System.currentTimeMillis() > tokenExpiration) {
            String tokenJson = getToken();
            Integer expiresIn = Integer.parseInt(
                (String) ((JSONObject) JSONValue.parse(tokenJson))
                    .get("expires_in"));
            tokenExpiration =
                System.currentTimeMillis() + ((expiresIn * 1000) - 1);
            token = "Bearer " + ((JSONObject) JSONValue.parse(tokenJson))
                .get("access_token");
            log.debug("New token:" + token);
        }
    }

    /**
     * Return raw response from Microsoft API
     */
    protected static String requestTranslations(MSTranslateArrayReq req)
            throws Exception {
        getTokenIfNeeded();

        ResteasyWebTarget webTarget =
                new ResteasyClientBuilder().build()
                        .target(TRANSLATIONS_BASE_URL);
        Response response = webTarget.request(MediaType.TEXT_XML)
                .header("Content-Type",
                        MediaType.TEXT_XML + "; charset=" + ENCODING)
                .header("Authorization", token)
                .post(Entity.xml(DTOUtil.toXML(req)));

        if (response.getStatusInfo() != Response.Status.OK) {
            throw new TranslationEngineException(
                response.getStatusInfo().toString(),
                "Error from Microsoft Translator API");
        }
        String xml = response.readEntity(String.class);
        response.close();
        log.info("Translation from Microsoft Engine:" + xml);
        return xml;
    }

    /**
     * Get access token from MS API
     */
    protected static String getToken() throws Exception {
        Response response = null;
        try {
            log.info("Getting token for Microsoft Engine");
            Invocation.Builder builder = new ResteasyClientBuilder()
                .build()
                .target(DATA_MARKET_ACCESS_URI)
                .request()
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .header("Accept-Charset", ENCODING);

            final String params = getTokenParam();

            response = builder.post(Entity
                .entity(params, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

            if (response.getStatusInfo() != Response.Status.OK) {
                throw new TranslationEngineException(
                    response.getStatusInfo().toString(), "Error getting token");
            }
            return response.readEntity(String.class);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    protected static String getTokenParam()
        throws UnsupportedEncodingException {
        Map<String, String> valuesMap = Maps.newHashMap();
        valuesMap.put("clientId",
            URLEncoder.encode(getClientId(), ENCODING));
        valuesMap.put("clientSecret",
            URLEncoder.encode(getSecret(), ENCODING));

        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        return sub.replace(templateTokenParam);
    }

    /**
     * @return Client id from system property: AZURE_ID
     */
    public static String getClientId() {
        return System.getProperty(AZURE_ID);
    }

    /**
     * @return Client secret from system property: AZURE_SECRET
     */
    public static String getSecret() {
        return System.getProperty(AZURE_SECRET);
    }

    /**
     * @throws TranslationEngineException if AZURE_ID or AZURE_SECRET is blank
     */
    public static void verifyCredentials() throws TranslationEngineException {
        if (StringUtils.isBlank(getClientId())
            || StringUtils.isBlank(getSecret())) {
            throw new TranslationEngineException(
                "Missing environment variables of AZURE_ID and AZURE_SECRET",
                "Missing required AZURE credentials");
        }
    }
}
