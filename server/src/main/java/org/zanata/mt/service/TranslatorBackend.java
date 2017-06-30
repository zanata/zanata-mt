package org.zanata.mt.service;

import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

import org.zanata.mt.api.dto.LocaleCode;
import org.zanata.mt.backend.BackendLocaleCode;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.AugmentedTranslation;

/**
 * Interface for machine translation provider
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface TranslatorBackend {

    /**
     * Return translation from MT provider
     * @throws ZanataMTException
     */
    AugmentedTranslation translate(String content, BackendLocaleCode srcLocale,
            BackendLocaleCode targetLocale, MediaType mediaType,
            Optional<String> category)
            throws ZanataMTException;

    /**
     * Return translations (same index as request) from MT provider
     * @throws ZanataMTException
     */
    List<AugmentedTranslation> translate(List<String> contents,
            BackendLocaleCode srcLocale,
            BackendLocaleCode targetLocale, MediaType mediaType,
            Optional<String> category)
            throws ZanataMTException;

    /**
     * Return mapped locale for the backend
     * @param localeCode
     */
    BackendLocaleCode getMappedLocale(@NotNull LocaleCode localeCode);
}
