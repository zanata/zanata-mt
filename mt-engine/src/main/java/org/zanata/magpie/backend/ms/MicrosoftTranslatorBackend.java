package org.zanata.magpie.backend.ms;

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
import org.zanata.magpie.annotation.Credentials;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCodeImpl;
import org.zanata.magpie.backend.BackendLocaleCode;
import org.zanata.magpie.backend.ms.internal.dto.MSString;
import org.zanata.magpie.backend.ms.internal.dto.MSTranslateArrayReq;
import org.zanata.magpie.backend.ms.internal.dto.MSTranslateArrayResp;
import org.zanata.magpie.backend.ms.internal.dto.MSTranslateArrayReqOptions;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.AugmentedTranslation;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.service.TranslatorBackend;
import org.zanata.magpie.util.DTOUtil;

/**
 * Service for Microsoft translator. {@link org.zanata.magpie.model.BackendID#MS}
 *
 * {@link org.zanata.magpie.api.APIConstant#AZURE_KEY} during startup.
 *
 * See
 * {@link #translate(List, BackendLocaleCode, BackendLocaleCode, MediaType, Optional)}
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
     * https://docs.microsoft.com/en-us/azure/cognitive-services/translator/languages
     *
     * Old list: http://web.archive.org/web/20171230230133/https://msdn.microsoft.com/en-us/library/hh456380.aspx
     */
    private final ImmutableMap<LocaleCode, BackendLocaleCode> LOCALE_MAP =
            ImmutableMap.of(
                    LocaleCode.ZH_HANS, new BackendLocaleCodeImpl("zh-CHS"),
                    LocaleCode.ZH_HANT, new BackendLocaleCodeImpl("zh-CHT")
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
            @Observes @Initialized(ApplicationScoped.class) Object init)
            throws MTException {
        api = new MicrosoftTranslatorClient(clientSubscriptionKey, restClient);
    }

    @Override
    public List<AugmentedTranslation> translate(List<String> contents,
            BackendLocaleCode fromLocale,
            BackendLocaleCode toLocale, MediaType mediaType,
            Optional<String> category)
            throws MTException {
        try {
            MSTranslateArrayReq req = new MSTranslateArrayReq();
            req.setSrcLanguage(fromLocale.getLocaleCode());
            req.setTransLanguage(toLocale.getLocaleCode());
            for (String content: contents) {
                req.getTexts().add(new MSString(content));
            }
            MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
            options.setContentType(mediaType.toString());
            category.ifPresent(options::setCategory);
            req.setOptions(options);

            String rawResponse = api.requestTranslations(req);
            MSTranslateArrayResp resp =
                    DTOUtil.toObject(rawResponse, MSTranslateArrayResp.class);
            return resp.getResponse().stream().map(
                    res -> new AugmentedTranslation(res.getTranslatedText().getValue(),
                            DTOUtil.toXML(res)))
                    .collect(Collectors.toList());
        } catch (JAXBException e) {
            throw new MTException("Unable to get translations from MS API", e);
        }
    }

    @Override
    public BackendLocaleCode getMappedLocale(@NotNull LocaleCode localeCode) {
        BackendLocaleCode from = new BackendLocaleCodeImpl(localeCode);
        return LOCALE_MAP.getOrDefault(localeCode, from);
    }

    @Override
    public int getCharLimitPerRequest() {
        return MAX_LENGTH;
    }

    @Override
    public BackendID getId() {
        return BackendID.MS;
    }

    @Override
    public Optional<List<BackendLocaleCode>> getSupportedLocales() {
        return Optional.empty();
    }

    @VisibleForTesting
    protected void setApi(MicrosoftTranslatorClient api) {
        this.api = api;
    }
}
