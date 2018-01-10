/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.magpie.mt.backend.google;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.magpie.mt.annotation.Credentials;
import org.magpie.mt.api.dto.LocaleCode;
import org.magpie.mt.backend.BackendLocaleCode;
import org.magpie.mt.backend.google.internal.dto.GoogleLocaleCode;
import org.magpie.mt.exception.MTException;
import org.magpie.mt.model.AugmentedTranslation;
import org.magpie.mt.model.BackendID;
import org.magpie.mt.service.TranslatorBackend;
import org.magpie.mt.util.DTOUtil;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GoogleTranslatorBackend implements TranslatorBackend {

    // Max length per request for Google Cloud Translation API
    private final static int MAX_LENGTH = 5000;

    private Translate translate;

    /**
     * Map from request locale to Google Cloud Translation API supported locale code
     *
     * https://cloud.google.com/translate/docs/languages
     */
    private final ImmutableMap<LocaleCode, GoogleLocaleCode> LOCALE_MAP =
            ImmutableMap.of(
                    LocaleCode.EN_US, new GoogleLocaleCode(LocaleCode.EN),
                    LocaleCode.ZH_HANS, new GoogleLocaleCode("zh-CN"),
                    LocaleCode.ZH_HANT, new GoogleLocaleCode("zh-TW")
            );
    private DTOUtil dtoUtil;
    private GoogleCredential googleCredential;

    @Inject
    public GoogleTranslatorBackend(DTOUtil dtoUtil,
            @Credentials(BackendID.GOOGLE) GoogleCredential googleCredential) {
        this.dtoUtil = dtoUtil;
        this.googleCredential = googleCredential;
        translate = TranslateOptions.getDefaultInstance().getService();
    }

    @VisibleForTesting
    protected GoogleTranslatorBackend(Translate translate,
            DTOUtil dtoUtil, GoogleCredential googleCredential) {
        this(dtoUtil, googleCredential);
        this.translate = translate;
    }

    @Override
    public List<AugmentedTranslation> translate(List<String> contents,
            BackendLocaleCode srcLocale, BackendLocaleCode targetLocale,
            MediaType mediaType, Optional<String> category) throws MTException {
        String format = MediaType.TEXT_HTML_TYPE.isCompatible(mediaType) ? "html" : "text";
        List<Translate.TranslateOption> options = Lists.newLinkedList();
        options.add(Translate.TranslateOption
                .targetLanguage(
                        targetLocale.getLocaleCode()));
        options.add(Translate.TranslateOption.format(format));
        if (!googleCredential.exists()) {
            throw new BadRequestException("Google Default Credential file is not setup");
        }
        // google can detect source locale if omitted
        // TODO we should probably retrieve and cache a google supported language list and check if the given locale code is supported or not
//        srcLocale.ifPresent(l -> options.add(
//                Translate.TranslateOption.sourceLanguage(l.getLocaleCode())));
        try {
            List<Translation> translations =
                    translate.translate(
                            contents,
                            options.toArray(new Translate.TranslateOption[options.size()]));
            return translations.stream()
                    .map(translation -> new AugmentedTranslation(
                            translation.getTranslatedText(),
                            dtoUtil.toJSON(translation))).collect(
                            Collectors.toList());
        } catch (Exception e) {
            throw new MTException(
                    "Unable to get translations from Google API", e);
        }
    }

    @Override
    public BackendLocaleCode getMappedLocale(LocaleCode localeCode) {
        GoogleLocaleCode googleLocaleCode = new GoogleLocaleCode(localeCode);
        return LOCALE_MAP.getOrDefault(localeCode, googleLocaleCode);
    }

    @Override
    public int getCharLimitPerRequest() {
        return MAX_LENGTH;
    }

    @Override
    public BackendID getId() {
        return BackendID.GOOGLE;
    }
}
