package org.zanata.mt.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.dao.TextFlowDAO;
import org.zanata.mt.dao.TextFlowTargetDAO;
import org.zanata.mt.exception.BadTranslationRequestException;
import org.zanata.mt.exception.TranslationEngineException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.Provider;
import org.zanata.mt.model.TextFlow;
import org.zanata.mt.model.TextFlowTarget;
import org.zanata.mt.service.impl.MicrosoftEngine;
import org.zanata.mt.util.TranslationUtil;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class TranslationService {
    private static final Logger log =
        LoggerFactory.getLogger(TranslationService.class);

    @Inject
    private TextFlowDAO textFlowDAO;

    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;

    private final TranslationEngine microsoftEngine = new MicrosoftEngine();

    // Max length for single string in Microsoft Engine
    private static final int MAX_LENGTH = 6000;

    // Max length before logging warning
    private static final int MAX_LENGTH_WARN = 3000;

    public void onStartUp(
            @Observes @Initialized(ApplicationScoped.class) Object init)
        throws TranslationEngineException {
        log.info("============================================");
        log.info("============================================");
        log.info("=====Zanata Machine Translation Service=====");
        log.info("============================================");
        log.info("============================================");

        try {
            microsoftEngine.init();
        } catch (TranslationEngineException e) {
            log.error("Error initialising translations engine:", e);
            throw e;
        }
    }

    /**
     * Translate single string in an api trigger
     *
     * Get from database if exists (hash), or from MT engine
     */
    @TransactionAttribute
    public String translate(@NotNull @Size(max = MAX_LENGTH) String string,
            @NotNull Locale srcLocale, @NotNull Locale targetLocale,
            @NotNull Provider provider)
            throws TranslationEngineException, BadTranslationRequestException {
        List<String> translations = translate(Lists.newArrayList(string),
                srcLocale, targetLocale, provider);
        return translations.get(0);
    }

    /**
     * Translate multiple string in an api trigger
     *
     * Get from database if exists (hash), or from MT engine
     */
    @TransactionAttribute
    public List<String> translate(@NotNull List<String> strings,
            @NotNull Locale srcLocale, @NotNull Locale targetLocale,
            @NotNull Provider provider)
        throws TranslationEngineException, BadTranslationRequestException {
        if (strings == null || strings.isEmpty() || srcLocale == null
                || targetLocale == null) {
            throw new BadTranslationRequestException();
        }
        int totalChar = strings.stream().mapToInt(String::length).sum();

        /**
         * return original string if it is more than MAX_LENGTH
         */
        if (totalChar > MAX_LENGTH) {
            log.warn("Requested string length is more than " + MAX_LENGTH);
            return strings;
        }
        if (totalChar > MAX_LENGTH_WARN) {
            log.warn("Requested string length is more than " + MAX_LENGTH_WARN);
        }

        List<String> results = new ArrayList<>(strings);
        Map<String, Integer> untranslatedIndexMap = Maps.newHashMap();
        Map<Integer, TextFlow> indexTextFlowMap = Maps.newHashMap();

        // search from database
        for (String string: strings) {
            String hash =
                    TranslationUtil.generateHash(string, srcLocale.getLocaleId());
            TextFlow matchedHashTf = textFlowDAO.getByHash(hash);

            int index = strings.indexOf(string);

            if (matchedHashTf != null) {
                Optional<TextFlowTarget> matchedTarget = getTargetByProvider(
                        matchedHashTf.getTargetsByLocaleId(
                                targetLocale.getLocaleId()),
                        provider);

                if (matchedTarget.isPresent()) {
                    TextFlowTarget matchedEntity = matchedTarget.get();
                    matchedEntity.incrementCount();
                    textFlowTargetDAO.persist(matchedEntity);
                    log.info(
                        "Found matched, Source-" + srcLocale.getLocaleId() + ":" +
                            string + "\nTranslation-" + targetLocale.getLocaleId() +
                            ":" + matchedEntity.getContent());
                    results.set(index, matchedEntity.getContent());
                } else {
                    untranslatedIndexMap.put(string, index);
                    indexTextFlowMap.put(index, matchedHashTf);
                }
            } else {
                untranslatedIndexMap.put(string, index);
                indexTextFlowMap.put(index, null);
            }
        }

        // no need of machine translations
        if (untranslatedIndexMap.isEmpty()) {
            return results;
        }

        // trigger MT engine search
        List<String> sources = Lists.newArrayList(untranslatedIndexMap.keySet());
        String response =
            microsoftEngine.translate(sources, srcLocale, targetLocale);
        List<String> translations =
            microsoftEngine.extractTranslations(response);
        List<String> rawXML =
            microsoftEngine.extractRawXML(response);

        for (String source: sources) {
            int index = untranslatedIndexMap.get(source);
            String translation = translations.get(sources.indexOf(source));
            String xml = rawXML.get(sources.indexOf(source));
            results.set(index, translation);

            TextFlow tf = indexTextFlowMap.get(index);
            if (tf == null) {
                tf = textFlowDAO.persist(new TextFlow(source, srcLocale));
            }
            TextFlowTarget target = new TextFlowTarget(translation, xml, tf,
                    targetLocale, provider);
            target = textFlowTargetDAO.persist(target);
            tf.getTargets().add(target);
        }
        return results;
    }

    private Optional<TextFlowTarget> getTargetByProvider(
            List<TextFlowTarget> targets, Provider provider) {
        for (TextFlowTarget target : targets) {
            if (target.getProvider().equals(provider)) {
                return Optional.of(target);
            }
        }
        return Optional.empty();
    }
}
