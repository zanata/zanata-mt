package org.zanata.magpie.service;

import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCode;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.AugmentedTranslation;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.StringType;

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
            BackendLocaleCode targetLocale, StringType stringType,
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
