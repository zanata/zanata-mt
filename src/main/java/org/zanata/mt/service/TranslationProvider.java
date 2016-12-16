package org.zanata.mt.service;

import java.util.List;

import org.zanata.mt.exception.TranslationProviderException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.ValueUnit;

/**
 * Interface for translation provider
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface TranslationProvider {

    /**
     * Return translation from MT provider
     * @throws TranslationProviderException
     */
    ValueUnit translate(String message, Locale srcLocale,
            Locale targetLocale) throws TranslationProviderException;

    /**
     * Return translations (same index as request) from MT provider
     * @throws TranslationProviderException
     */
    List<ValueUnit> translate(List<String> messages, Locale srcLocale,
        Locale targetLocale) throws TranslationProviderException;

    /**
     * Init method when initialised
     */
    void init() throws TranslationProviderException;
}
