package org.zanata.mt.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.zanata.mt.api.dto.Microsoft.MSString;
import org.zanata.mt.api.dto.Microsoft.MSTranslateArrayReq;
import org.zanata.mt.api.dto.Microsoft.MSTranslateArrayResp;
import org.zanata.mt.api.dto.Microsoft.Options;
import org.zanata.mt.exception.TranslationEngineException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.service.MicrosoftTranslatorAPI;
import org.zanata.mt.service.TranslationEngine;
import org.zanata.mt.util.DTOUtil;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MicrosoftEngine extends MicrosoftTranslatorAPI
    implements TranslationEngine {

    @Override
    public void init() throws TranslationEngineException {
        verifyCredentials();
    }

    @Override
    public String translate(String message, Locale srcLocale,
            Locale targetLocale) throws TranslationEngineException {
        return translate(Lists.newArrayList(message), srcLocale, targetLocale);
    }

    @Override
    public String translate(List<String> messages, Locale srcLocale,
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
            return requestTranslations(req);
        } catch (Exception e) {
            throw new TranslationEngineException(e);
        }
    }

    @Override
    public List<String> extractTranslations(String xml)
            throws TranslationEngineException {
        try {
            MSTranslateArrayResp resp =
                    DTOUtil.toObject(xml, MSTranslateArrayResp.class);
            return resp.getResponse().stream()
                .map(res -> res.getTranslatedText().getValue())
                .collect(Collectors.toList());
        } catch (JAXBException e) {
            throw new TranslationEngineException(e);
        }
    }

    @Override
    public List<String> extractRawXML(String xml)
        throws TranslationEngineException {
        try {
            MSTranslateArrayResp resp =
                DTOUtil.toObject(xml, MSTranslateArrayResp.class);
            return resp.getResponse().stream().map(res -> DTOUtil.toXML(res))
                    .collect(Collectors.toList());
        } catch (JAXBException e) {
            throw new TranslationEngineException(e);
        }
    }
}
