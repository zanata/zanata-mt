package org.zanata.mt.service;

import java.util.List;

import org.zanata.mt.exception.TranslationEngineException;
import org.zanata.mt.model.Locale;

/**
 * Interface for translation engine
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface TranslationEngine {

    /**
     * Return raw response from MT provider
     * @throws TranslationEngineException
     */
    String translate(String message, Locale srcLocale,
            Locale targetLocale) throws TranslationEngineException;

    /**
     * Return raw response from MT provider
     * @throws TranslationEngineException
     */
    String translate(List<String> messages, Locale srcLocale,
        Locale targetLocale) throws TranslationEngineException;

    /**
     * Extract translations from raw response from MT provider
     * @throws TranslationEngineException
     */
    List<String> extractTranslations(String response) throws
        TranslationEngineException;

    /**
     * Extract translations from raw response from MT provider
     * @throws TranslationEngineException
     */
    List<String> extractRawXML(String response) throws
        TranslationEngineException;

    /**
     * Init method when initialised
     */
    void init() throws TranslationEngineException;
}
