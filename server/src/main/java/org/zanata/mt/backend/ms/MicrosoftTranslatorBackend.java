package org.zanata.mt.backend.ms;

import java.util.List;
import java.util.Optional;
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
import org.zanata.mt.annotation.Credentials;
import org.zanata.mt.annotation.DevMode;
import org.zanata.mt.api.dto.LocaleCode;
import org.zanata.mt.backend.BackendLocaleCode;
import org.zanata.mt.backend.ms.internal.dto.MSLocaleCode;
import org.zanata.mt.backend.ms.internal.dto.MSString;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayReq;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayResp;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayReqOptions;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.service.TranslatorBackend;
import org.zanata.mt.util.DTOUtil;

import com.google.common.collect.Lists;

import static org.zanata.mt.api.APIConstant.AZURE_KEY;

/**
 * Service for Microsoft translator. {@link org.zanata.mt.model.BackendID#MS}
 *
 * {@link org.zanata.mt.api.APIConstant#AZURE_KEY} during startup.
 *
 * See
 * {@link #translate(String, BackendLocaleCode, BackendLocaleCode, MediaType)}
 * {@link #translate(List, BackendLocaleCode, BackendLocaleCode, MediaType)}
 *
 * See {@link MicrosoftTranslatorClient} for MS translator configuration.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class MicrosoftTranslatorBackend implements TranslatorBackend {

    // Max length per request for MS service
    private final static int MAX_LENGTH = 10000;

    /**
     * Map from request locale to MS supported locale code
     *
     * https://msdn.microsoft.com/en-us/library/hh456380.aspx
     */
    private final ImmutableMap<LocaleCode, MSLocaleCode> LOCALE_MAP =
            ImmutableMap.of(
                    LocaleCode.ZH_HANS, new MSLocaleCode("zh-CHS"),
                    LocaleCode.ZH_HANT, new MSLocaleCode("zh-CHT")
            );

    private String clientSubscriptionKey;

    private MicrosoftTranslatorClient api;

    private final MicrosoftRestEasyClient
            restClient = new MicrosoftRestEasyClient();

    @SuppressWarnings("unused")
    public MicrosoftTranslatorBackend() {
    }

    @Inject
    public MicrosoftTranslatorBackend(@Credentials(BackendID.MS) String msAPIKey) {
        this.clientSubscriptionKey = msAPIKey;
    }

    public void onInit(
            @Observes @Initialized(ApplicationScoped.class) Object init, @DevMode boolean isDevMode)
            throws ZanataMTException {
        if (!isDevMode &&
                StringUtils.isBlank(clientSubscriptionKey)) {
            throw new ZanataMTException(
                    "Missing system properties of " + AZURE_KEY);
        }
        api = new MicrosoftTranslatorClient(clientSubscriptionKey, restClient);
    }

    @Override
    public AugmentedTranslation translate(String content,
            BackendLocaleCode srcLocale,
            BackendLocaleCode targetLocale, MediaType mediaType,
            Optional<String> category)
            throws ZanataMTException {
        return translate(Lists.newArrayList(content), Optional.of(srcLocale), targetLocale,
                mediaType, category).get(0);
    }

    @Override
    public List<AugmentedTranslation> translate(List<String> contents,
            Optional<BackendLocaleCode> fromLocale,
            BackendLocaleCode toLocale, MediaType mediaType,
            Optional<String> category)
            throws ZanataMTException {
        try {
            MSTranslateArrayReq req = new MSTranslateArrayReq();
            // see getMappedLocale() we always return what's provided by the user
            req.setSrcLanguage(fromLocale.get().getLocaleCode());
            req.setTransLanguage(toLocale.getLocaleCode());
            for (String content: contents) {
                req.getTexts().add(new MSString(content));
            }
            MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
            options.setContentType(mediaType.toString());
            if (category.isPresent()) {
                options.setCategory(category.get());
            }
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

    @Override
    public Optional<BackendLocaleCode> getMappedLocale(@NotNull LocaleCode localeCode) {
        MSLocaleCode from = new MSLocaleCode(localeCode);
        return Optional.of(LOCALE_MAP.getOrDefault(localeCode, from));
    }

    @Override
    public int getCharLimitPerRequest() {
        return MAX_LENGTH;
    }

    @Override
    public BackendID getId() {
        return BackendID.MS;
    }

    @VisibleForTesting
    protected void setApi(MicrosoftTranslatorClient api) {
        this.api = api;
    }
}
