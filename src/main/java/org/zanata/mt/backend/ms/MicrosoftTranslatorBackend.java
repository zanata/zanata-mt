package org.zanata.mt.backend.ms;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.zanata.mt.annotation.SystemProperty;
import org.zanata.mt.backend.ms.internal.dto.MSString;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayReq;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayResp;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayReqOptions;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.service.TranslatorBackend;
import org.zanata.mt.util.DTOUtil;

import com.google.common.collect.Lists;

import static org.zanata.mt.api.APIConstant.AZURE_ID;
import static org.zanata.mt.api.APIConstant.AZURE_SECRET;

/**
 * Service for Microsoft translator.
 * Checks for {@link org.zanata.mt.api.APIConstant#AZURE_ID} and
 * {@link org.zanata.mt.api.APIConstant#AZURE_SECRET} during startup.
 *
 *
 * See
 * {@link #translate(String, Locale, Locale, MediaType)} and
 * {@link #translate(List, Locale, Locale, MediaType)} for more info.
 *
 * See {@link MicrosoftTranslatorClient} for MS translator configuration.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class MicrosoftTranslatorBackend implements TranslatorBackend {

    public static final String ATTRIBUTION_REF = "http://aka.ms/MicrosoftTranslatorAttribution";

    private String clientId;

    private String clientSecret;

    private MicrosoftTranslatorClient api;

    //200X41
    private String base64Image;

    @SuppressWarnings("unused")
    public MicrosoftTranslatorBackend() {
    }

    @Inject
    public MicrosoftTranslatorBackend(@SystemProperty(AZURE_ID) String clientId,
        @SystemProperty(AZURE_SECRET) String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public void onInit(@Observes @Initialized(ApplicationScoped.class) Object init)
        throws ZanataMTException {
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
            throw new ZanataMTException(
                "Missing system properties of" + AZURE_ID + " and " + AZURE_SECRET);
        }
        api = new MicrosoftTranslatorClient(clientId, clientSecret);

        ClassLoader classLoader = getClass().getClassLoader();
        try {
            base64Image = IOUtils.toString(classLoader.getResourceAsStream("ms-attribution.uri"),
                    CharEncoding.UTF_8);
        } catch (IOException e) {
            throw new ZanataMTException("Unable to load MS attribution image", e);
        }
    }

    @Override
    public AugmentedTranslation translate(String content, Locale srcLocale,
            Locale targetLocale, MediaType mediaType) throws ZanataMTException {
        return translate(Lists.newArrayList(content), srcLocale, targetLocale,
                mediaType).get(0);
    }

    @Override
    public List<AugmentedTranslation> translate(List<String> contents, Locale srcLocale,
        Locale targetLocale, MediaType mediaType) throws ZanataMTException {
        try {
            MSTranslateArrayReq req = new MSTranslateArrayReq();
            req.setSrcLanguage(srcLocale.getLocaleId());
            req.setTransLanguage(targetLocale.getLocaleId());
            for (String content: contents) {
                req.getTexts().add(new MSString(content));
            }
            MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
            options.setContentType(mediaType.toString());
            req.setOptions(options);

            String rawResponse = api.requestTranslations(req);
            MSTranslateArrayResp resp =
                    DTOUtil.toObject(rawResponse, MSTranslateArrayResp.class);
            return resp.getResponse().stream().map(
                    res -> new AugmentedTranslation(res.getTranslatedText().getValue(),
                            DTOUtil.toXML(res)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ZanataMTException("Unable to get translations from MS API", e);
        }
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getAttributionSmall() {
        StringBuilder sb = new StringBuilder();

        sb.append("<a href='")
                .append(ATTRIBUTION_REF)
                .append("'>")
                .append("<img src='")
                .append(base64Image)
                .append("'/>")
                .append("</a>");

        return sb.toString();
    }
}
