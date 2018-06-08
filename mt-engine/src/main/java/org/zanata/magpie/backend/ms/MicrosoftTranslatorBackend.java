package org.zanata.magpie.backend.ms;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.zanata.magpie.annotation.Credentials;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCodeImpl;
import org.zanata.magpie.backend.BackendLocaleCode;
import org.zanata.magpie.backend.ms.internal.dto.MSString;
import org.zanata.magpie.backend.ms.internal.dto.MSTranslateArrayReq;
import org.zanata.magpie.backend.ms.internal.dto.MSTranslateJSONArrayResp;
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
    private final static int MAX_LENGTH = 5_000;

    /**
     * Map from request locale to MS supported locale code
     *
     * https://msdn.microsoft.com/en-us/library/hh456380.aspx
     */
    private final ImmutableMap<LocaleCode, BackendLocaleCode> LOCALE_MAP =
            ImmutableMap.of(
                    LocaleCode.ZH_HANS, new BackendLocaleCodeImpl("zh-CHS"),
                    LocaleCode.ZH_HANT, new BackendLocaleCodeImpl("zh-CHT")
            );

    private String clientSubscriptionKey;
    private DTOUtil dtoUtil;

    private MicrosoftTranslatorClient api;

    private final MicrosoftRestEasyClient
            restClient = new MicrosoftRestEasyClient();

    @SuppressWarnings("unused")
    public MicrosoftTranslatorBackend() {
    }

    @Inject
    public MicrosoftTranslatorBackend(@Credentials(BackendID.MS) String msAPIKey, DTOUtil dtoUtil) {
        this.clientSubscriptionKey = msAPIKey;
        this.dtoUtil = dtoUtil;
    }

    public void onInit(
            @Observes @Initialized(ApplicationScoped.class) Object init)
            throws MTException {
        api = new MicrosoftTranslatorClient(clientSubscriptionKey, restClient, dtoUtil);
    }

    @Override
    public List<AugmentedTranslation> translate(List<String> contents,
            BackendLocaleCode fromLocale,
            BackendLocaleCode toLocale, MediaType mediaType,
            Optional<String> category)
            throws MTException {
        try {
            MSTranslateArrayReq req = new MSTranslateArrayReq();
            // TODO MS has restriction of max 25 entries in the list
            for (String content: contents) {
                req.getTexts().add(new MSString(content));
            }
            String rawResponse = api.requestTranslations(req, fromLocale.getLocaleCode(), toLocale.getLocaleCode(), category, mediaType);


            List<MSTranslateJSONArrayResp> resp =
                    dtoUtil.fromJSONToObjectList(rawResponse,
                            new TypeReference<List<MSTranslateJSONArrayResp>>() {
                            });
            return resp.stream().map(
                    // we only have one text entry per translation
                    translation -> new AugmentedTranslation(translation.getTranslations()
                            .get(0).getText(),
                            dtoUtil.toJSON(translation)))
                    .collect(Collectors.toList());
        } catch (IOException e) {
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

    @VisibleForTesting
    protected void setApi(MicrosoftTranslatorClient api) {
        this.api = api;
    }
}
