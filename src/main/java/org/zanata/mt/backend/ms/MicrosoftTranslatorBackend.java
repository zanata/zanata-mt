package org.zanata.mt.backend.ms;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.zanata.mt.annotation.EnvVariable;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.backend.BackendLocaleCode;
import org.zanata.mt.backend.ms.internal.dto.MSLocaleCode;
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

import static org.zanata.mt.api.APIConstant.AZURE_KEY;

/**
 * Service for Microsoft translator.
 * {@link org.zanata.mt.api.APIConstant#AZURE_KEY} during startup.
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

    /**
     * Map from request locale to MS supported locale code
     *
     * https://msdn.microsoft.com/en-us/library/hh456380.aspx
     */
    private final ImmutableMap<LocaleId, MSLocaleCode> LOCALE_MAP =
            ImmutableMap.of(
                    LocaleId.ZH_HANS, new MSLocaleCode("zh-CHS"),
                    LocaleId.ZH_HANT, new MSLocaleCode("zh-CHT")
            );

    private String clientSubscriptionKey;

    private MicrosoftTranslatorClient api;

    private final MicrosoftRestEasyClient
            restClient = new MicrosoftRestEasyClient();

    @SuppressWarnings("unused")
    public MicrosoftTranslatorBackend() {
    }

    @Inject
    public MicrosoftTranslatorBackend(
        @EnvVariable(AZURE_KEY) String clientSubscriptionKey) {
        this.clientSubscriptionKey = clientSubscriptionKey;
    }

    public void onInit(@Observes @Initialized(ApplicationScoped.class) Object init)
        throws ZanataMTException {
        if (StringUtils.isBlank(clientSubscriptionKey)) {
            throw new ZanataMTException(
                "Missing system properties of " + AZURE_KEY);
        }
        api = new MicrosoftTranslatorClient(clientSubscriptionKey, restClient);
    }

    @Override
    public AugmentedTranslation translate(String content,
            BackendLocaleCode srcLocale,
            BackendLocaleCode targetLocale, MediaType mediaType)
            throws ZanataMTException {
        return translate(Lists.newArrayList(content), srcLocale, targetLocale,
                mediaType).get(0);
    }

    @Override
    public List<AugmentedTranslation> translate(List<String> contents,
            BackendLocaleCode fromLocale,
            BackendLocaleCode toLocale, MediaType mediaType)
            throws ZanataMTException {
        try {
            MSTranslateArrayReq req = new MSTranslateArrayReq();
            req.setSrcLanguage(fromLocale.getLocaleCode());
            req.setTransLanguage(toLocale.getLocaleCode());
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
        } catch (JAXBException e) {
            throw new ZanataMTException("Unable to get translations from MS API", e);
        }
    }

    public String getClientSubscriptionKey() {
        return clientSubscriptionKey;
    }

    @Override
    public BackendLocaleCode getMappedLocale(@NotNull LocaleId localeId) {
        MSLocaleCode from = new MSLocaleCode(localeId);
        return LOCALE_MAP.getOrDefault(localeId, from);
    }

    @VisibleForTesting
    protected void setApi(MicrosoftTranslatorClient api) {
        this.api = api;
    }
}
