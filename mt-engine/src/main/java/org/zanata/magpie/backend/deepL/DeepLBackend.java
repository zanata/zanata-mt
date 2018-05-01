/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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

package org.zanata.magpie.backend.deepL;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.zanata.magpie.annotation.Credentials;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCode;
import org.zanata.magpie.backend.deepL.internal.dto.DeepLLocaleCode;
import org.zanata.magpie.backend.deepL.internal.dto.DeepLResp;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.AugmentedTranslation;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.service.TranslatorBackend;
import org.zanata.magpie.util.DTOUtil;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DeepLBackend implements TranslatorBackend {
    // Max length per request for DeepL: https://www.deepl.com/api.html
    private final static int MAX_LENGTH = 30000;

    private DeepLClient deepLClient = new DeepLClient();

    private String authKey;
    /**
     * Map from request locale to DeepL supported locale code
     *
     * https://www.deepl.com/api.html
     */
    private final ImmutableMap<LocaleCode, DeepLLocaleCode> LOCALE_MAP =
            ImmutableMap.of(
                    LocaleCode.EN_US, new DeepLLocaleCode(LocaleCode.EN)
            );

    private final ImmutableList<BackendLocaleCode> SUPPORTED_LOCALES =
            ImmutableList.of(
                    new DeepLLocaleCode(LocaleCode.EN),
                    new DeepLLocaleCode(LocaleCode.DE),
                    new DeepLLocaleCode(LocaleCode.FR),
                    new DeepLLocaleCode(LocaleCode.ES),
                    new DeepLLocaleCode(LocaleCode.IT),
                    new DeepLLocaleCode(LocaleCode.NL),
                    new DeepLLocaleCode(LocaleCode.PL)
            );

    @SuppressWarnings("unused")
    public DeepLBackend() {
    }

    @Inject
    public DeepLBackend(@Credentials(BackendID.DEEPL) String deepLKey) {
        this.authKey = deepLKey;
    }

    @Override
    public List<AugmentedTranslation> translate(List<String> contents,
            BackendLocaleCode srcLocale, BackendLocaleCode targetLocale,
            MediaType mediaType, Optional<String> category) throws MTException {
        DeepLResp resp = deepLClient
                .requestTranslations(contents, srcLocale, targetLocale,
                        authKey);
        return resp.getTranslations().stream()
                .map(translation -> new AugmentedTranslation(
                        translation.getTranslation(),
                        DTOUtil.toJSON(translation))).collect(
                        Collectors.toList());
    }

    @Override
    public BackendLocaleCode getMappedLocale(LocaleCode localeCode) {
        DeepLLocaleCode deepLLocaleCode = new DeepLLocaleCode(localeCode);
        return LOCALE_MAP.getOrDefault(localeCode, deepLLocaleCode);
    }

    @Override
    public int getCharLimitPerRequest() {
        return MAX_LENGTH;
    }

    @Override
    public BackendID getId() {
        return BackendID.DEEPL;
    }

    @Override
    public Optional<List<BackendLocaleCode>> getSupportedLocales() {
        return Optional.of(SUPPORTED_LOCALES);
    }

    @VisibleForTesting
    protected void setClient(DeepLClient client) {
        this.deepLClient = client;
    }
}
