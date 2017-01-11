package org.zanata.mt.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.zanata.mt.annotation.SystemProperty;
import org.zanata.mt.api.dto.microsoft.MSString;
import org.zanata.mt.api.dto.microsoft.MSTranslateArrayReq;
import org.zanata.mt.api.dto.microsoft.MSTranslateArrayResp;
import org.zanata.mt.api.dto.microsoft.MSTranslateArrayReqOptions;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.service.TranslationProvider;
import org.zanata.mt.util.DTOUtil;

import com.google.common.collect.Lists;

/**
 * Microsoft Provider for machine translation
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class MicrosoftProvider implements TranslationProvider {

    public static final String AZURE_ID = "AZURE_ID";
    public static final String AZURE_SECRET = "AZURE_SECRET";

    private String clientId;

    private String clientSecret;

    private MicrosoftTranslatorAPI api;

    @SuppressWarnings("unused")
    public MicrosoftProvider() {
    }

    @Inject
    public MicrosoftProvider(@SystemProperty(AZURE_ID) String clientId,
        @SystemProperty(AZURE_SECRET) String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public void init() throws ZanataMTException {
        if (StringUtils.isBlank(clientId)
            || StringUtils.isBlank(clientSecret)) {
            throw new ZanataMTException(
                "Missing environment variables of AZURE_ID and AZURE_SECRET");
        }
        api = new MicrosoftTranslatorAPI(clientId, clientSecret);
    }

    @Override
    public AugmentedTranslation translate(String message, Locale srcLocale,
            Locale targetLocale, MediaType mediaType) throws ZanataMTException {
        return translate(Lists.newArrayList(message), srcLocale, targetLocale,
                mediaType).get(0);
    }

    @Override
    public List<AugmentedTranslation> translate(List<String> messages, Locale srcLocale,
        Locale targetLocale, MediaType mediaType) throws ZanataMTException {
        try {
            MSTranslateArrayReq req = new MSTranslateArrayReq();
            req.setSrcLanguage(srcLocale.getLocaleId());
            req.setTransLanguage(targetLocale.getLocaleId());
            for (String message: messages) {
                req.getTexts().add(new MSString(message));
            }
            MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
            options.setContentType(mediaType.getType());
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
}
