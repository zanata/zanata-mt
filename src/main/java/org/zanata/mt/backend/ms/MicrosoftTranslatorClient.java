package org.zanata.mt.backend.ms;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayReq;
import org.zanata.mt.exception.ZanataMTException;
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
 * Client for Microsoft Translator service using RestEasy.
 * Uses the HTTP Interface V2 - see: https://msdn.microsoft.com/en-us/library/ff512422.aspx
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
class MicrosoftTranslatorClient {

    private static final Logger LOG =
        LoggerFactory.getLogger(MicrosoftTranslatorClient.class);

    private static final String TRANSLATIONS_BASE_URL = "http://api.microsofttranslator.com/V2/Http.svc/TranslateArray2";
    private static final String DATA_MARKET_ACCESS_URI = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";

    private static final String ENCODING = CharEncoding.UTF_8;

    private static final String TEMPLATE_TOKEN_PARAM =
        "grant_type=client_credentials&scope=http://api.microsofttranslator.com&client_id=${clientId}&client_secret=${clientSecret}";

    // The access token is valid for 10 minutes
    private long tokenExpiration = 0;
    private String token;

    private final String clientId;

    private final String clientSecret;

    protected MicrosoftTranslatorClient(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * Get access token from MS API if token is expired.
     * 10 minutes by default
     *
     * @throws Exception
     */
    protected void getTokenIfNeeded() {
        if (System.currentTimeMillis() > tokenExpiration) {
            String tokenJson = getToken();
            Integer expiresIn = Integer.parseInt(
                (String) ((JSONObject) JSONValue.parse(tokenJson))
                    .get("expires_in"));
            tokenExpiration =
                System.currentTimeMillis() + ((expiresIn * 1000) - 1);
            token = "Bearer " + ((JSONObject) JSONValue.parse(tokenJson))
                .get("access_token");
            if (LOG.isDebugEnabled()) {
                LOG.debug("New token:" + token);
            }
        }
    }

    /**
     * Return raw response from Microsoft API
     */
    protected String requestTranslations(MSTranslateArrayReq req)
            throws ZanataMTException {
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
            throw new ZanataMTException(
                    "Error from Microsoft Translator API:"
                            + response.getStatusInfo().toString());
        }
        String xml = response.readEntity(String.class);
        response.close();
        LOG.info("Translation from Microsoft Engine:" + xml);
        return xml;
    }

    /**
     * Get access token from MS API
     */
    protected String getToken() throws ZanataMTException {
        Response response = null;
        try {
            LOG.info("Getting token for Microsoft Engine");

            Invocation.Builder builder = getBuilder();
            final String params = getTokenParam();

            response = builder.post(Entity
                .entity(params, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

            if (response.getStatusInfo() != Response.Status.OK) {
                throw new ZanataMTException(
                        "Error getting token"
                                + response.getStatusInfo().toString());
            }
            return response.readEntity(String.class);
        } catch (UnsupportedEncodingException e) {
            throw new ZanataMTException("Unable to get system properties", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    protected Invocation.Builder getBuilder() {
        return new ResteasyClientBuilder()
            .build()
            .target(DATA_MARKET_ACCESS_URI)
            .request()
            .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
            .header("Accept-Charset", ENCODING);
    }

    protected String getTokenParam()
        throws UnsupportedEncodingException {
        Map<String, String> valuesMap = Maps.newHashMap();
        valuesMap.put("clientId",
            URLEncoder.encode(clientId, ENCODING));
        valuesMap.put("clientSecret",
            URLEncoder.encode(clientSecret, ENCODING));

        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        return sub.replace(TEMPLATE_TOKEN_PARAM);
    }
}
