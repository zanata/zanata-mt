package org.zanata.mt.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.dto.Microsoft.MSString;
import org.zanata.mt.api.dto.Microsoft.MSTranslateArrayReq;
import org.zanata.mt.api.dto.Microsoft.MSTranslateArrayResp;
import org.zanata.mt.api.dto.Microsoft.Options;
import org.zanata.mt.exception.TranslationEngineException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.service.MicrosoftTranslatorAPI;
import org.zanata.mt.service.TranslationEngine;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MicrosoftEngine extends MicrosoftTranslatorAPI
    implements TranslationEngine {
    private static final Logger log =
        LoggerFactory.getLogger(MicrosoftEngine.class);

    @Override
    public void init() throws TranslationEngineException {
        verifyCredentials();
    }

    @Override
    public String translate(String message, Locale srcLocale,
            Locale targetLocale) throws TranslationEngineException {
        List<String> translations =
                translate(Lists.newArrayList(message), srcLocale, targetLocale);
        return translations == null || translations.isEmpty() ? null
                : translations.get(0);
    }

    @Override
    public List<String> translate(List<String> messages, Locale srcLocale,
        Locale targetLocale) throws TranslationEngineException {
        try {
            MSTranslateArrayReq req = new MSTranslateArrayReq();
            req.setSrcLanguage(srcLocale.getLocaleId());
            req.setTransLanguage(targetLocale.getLocaleId());
            for (String message: messages) {
                req.getTexts().add(new MSString(message));
            }
            Options options = new Options();
            options.setContentType(MEDIA_TYPE);
            req.setOptions(options);

            MSTranslateArrayResp resp = requestTranslations(req);
            List<String> results = resp.getResponse().getTranslatedText().stream()
                .map(MSString::getValue).collect(Collectors.toList());
            return results;
        } catch (Exception e) {
            throw new TranslationEngineException(e);
        }
    }
}
