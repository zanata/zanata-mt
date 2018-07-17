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
package org.zanata.magpie.backend.google;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.zanata.magpie.annotation.Credentials;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCode;
import org.zanata.magpie.backend.BackendLocaleCodeImpl;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.AugmentedTranslation;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.service.TranslatorBackend;
import org.zanata.magpie.util.DTOUtil;

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
    // Max number of "text segments" that can be sent in a request
    private final static int BATCH_SIZE = 100;

    private Translate translate;

    /**
     * Map from request locale to Google Cloud Translation API supported locale code
     *
     * https://cloud.google.com/translate/docs/languages
     */
    private final ImmutableMap<LocaleCode, BackendLocaleCode> LOCALE_MAP =
            ImmutableMap.of(
                    LocaleCode.EN_US, new BackendLocaleCodeImpl(LocaleCode.EN),
                    LocaleCode.ZH_HANS, new BackendLocaleCodeImpl("zh-CN"),
                    LocaleCode.ZH_HANT, new BackendLocaleCodeImpl("zh-TW")
            );
    private DTOUtil dtoUtil;
    private GoogleCredential googleCredential;

    @Inject
    public GoogleTranslatorBackend(DTOUtil dtoUtil,
            @Credentials(BackendID.GOOGLE) GoogleCredential googleCredential) {
        this.dtoUtil = dtoUtil;
        this.googleCredential = googleCredential;
        if (googleCredential.exists()) {
            translate = TranslateOptions.getDefaultInstance().getService();
        }
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
            List<Translation> translations = new ArrayList<>();
            int batchStart = 0;
            while (batchStart < contents.size()) {
                int batchEnd = Math.min(batchStart + BATCH_SIZE, contents.size());
                translations.addAll(translate.translate(
                        contents.subList(batchStart, batchEnd),
                        options.toArray(
                                new Translate.TranslateOption[options.size()])));
                batchStart = batchEnd;
            }
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
        BackendLocaleCode googleLocaleCode = new BackendLocaleCodeImpl(localeCode);
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
