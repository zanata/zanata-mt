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
     * Returns single translated string
     * @throws TranslationEngineException
     */
    String translate(String message, Locale srcLocale,
            Locale targetLocale) throws TranslationEngineException;

    /**
     * Return list of translations that has same size as messages
     * @throws TranslationEngineException
     */
    List<String> translate(List<String> messages, Locale srcLocale,
        Locale targetLocale) throws TranslationEngineException;

    /**
     * Init method when initialised
     */
    void init() throws TranslationEngineException;
}
