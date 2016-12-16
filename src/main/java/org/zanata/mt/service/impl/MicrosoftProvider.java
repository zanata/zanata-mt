package org.zanata.mt.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.zanata.mt.api.dto.microsoft.MSString;
import org.zanata.mt.api.dto.microsoft.MSTranslateArrayReq;
import org.zanata.mt.api.dto.microsoft.MSTranslateArrayResp;
import org.zanata.mt.api.dto.microsoft.Options;
import org.zanata.mt.exception.TranslationProviderException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.ValueUnit;
import org.zanata.mt.service.TranslationProvider;
import org.zanata.mt.util.DTOUtil;

import com.google.common.collect.Lists;

/**
 * Microsoft Provider for machine translation
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MicrosoftProvider extends MicrosoftTranslatorAPI
    implements TranslationProvider {

    @Override
    public void init() throws TranslationProviderException {
        verifyCredentials();
    }

    @Override
    public ValueUnit translate(String message, Locale srcLocale,
            Locale targetLocale) throws TranslationProviderException {
        return translate(Lists.newArrayList(message), srcLocale, targetLocale)
                .get(0);
    }

    @Override
    public List<ValueUnit> translate(List<String> messages, Locale srcLocale,
        Locale targetLocale) throws TranslationProviderException {
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

            String rawResponse = requestTranslations(req);
            MSTranslateArrayResp resp =
                    DTOUtil.toObject(rawResponse, MSTranslateArrayResp.class);
            return resp.getResponse().stream().map(
                    res -> new ValueUnit(res.getTranslatedText().getValue(),
                            DTOUtil.toXML(res)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new TranslationProviderException(e);
        }
    }
}
