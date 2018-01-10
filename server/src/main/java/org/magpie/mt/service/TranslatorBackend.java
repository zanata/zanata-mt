package org.magpie.mt.service;

import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

import org.magpie.mt.api.dto.LocaleCode;
import org.magpie.mt.backend.BackendLocaleCode;
import org.magpie.mt.exception.MTException;
import org.magpie.mt.model.AugmentedTranslation;
import org.magpie.mt.model.BackendID;

/**
 * Interface for machine translation provider
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface TranslatorBackend {

    /**
     * Return translations (same index as request) from MT provider
     * @throws MTException
     */
    List<AugmentedTranslation> translate(List<String> contents,
            BackendLocaleCode srcLocale,
            BackendLocaleCode targetLocale, MediaType mediaType,
            Optional<String> category)
            throws MTException;

    /**
     * Return mapped locale for the backend
     * @param localeCode
     */
    BackendLocaleCode getMappedLocale(@NotNull LocaleCode localeCode);

    /**
     * @return
     *      max length for request for this backend. Contents will be segmented
     *      if the length of over limit
     */
    int getCharLimitPerRequest();

    BackendID getId();
}
