package org.zanata.mt.service;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.AugmentedTranslation;

/**
 * Interface for machine translation provider
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface TranslationProvider {

    /**
     * Return translation from MT provider
     * @throws ZanataMTException
     */
    AugmentedTranslation translate(String message, Locale srcLocale,
            Locale targetLocale, MediaType mediaType) throws ZanataMTException;

    /**
     * Return translations (same index as request) from MT provider
     * @throws ZanataMTException
     */
    List<AugmentedTranslation> translate(List<String> messages, Locale srcLocale,
        Locale targetLocale, MediaType mediaType) throws ZanataMTException;

    /**
     * Init method when initialised
     */
    void init() throws ZanataMTException;
}
