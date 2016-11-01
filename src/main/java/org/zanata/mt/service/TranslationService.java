package org.zanata.mt.service;

import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.commons.lang3.StringUtils;
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
    private static final int MAX_LENGTH = 10000;

    public void onStartUp(
            @Observes @Initialized(ApplicationScoped.class) Object init) {
        log.info("============================================");
        log.info("============================================");
        log.info("=====Zanata Machine Translation Service=====");
        log.info("============================================");
        log.info("============================================");

        try {
            microsoftEngine.init();
        } catch (TranslationEngineException e) {
            log.error("Error initialising translations engine:", e);
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
        if (StringUtils.isBlank(string) ||
            StringUtils.length(string) > MAX_LENGTH ||
            srcLocale == null || targetLocale == null || provider == null) {
            throw new BadTranslationRequestException();
        }
        String hash = TranslationUtil.generateHash(string, srcLocale.getLocaleId());
        TextFlow matchedHashTf = textFlowDAO.getByHash(hash);

        if (matchedHashTf != null) {
            Optional<TextFlowTarget> matchedTarget = getTargetByProvider(
                    matchedHashTf
                            .getTargetsByLocaleId(targetLocale.getLocaleId()),
                    provider);

            if (matchedTarget.isPresent()) {
                TextFlowTarget matchedTargetEntity = matchedTarget.get();
                matchedTargetEntity.incrementCount();
                textFlowTargetDAO.persist(matchedTargetEntity);
                log.info(
                    "Found matched, Source-" + srcLocale.getLocaleId() + ":" +
                        string + "\nTranslation-" + targetLocale.getLocaleId() +
                        ":" + matchedTargetEntity.getContent());
                return matchedTargetEntity.getContent();
            }
        }

        // fire MT engine search
        if (matchedHashTf == null) {
            matchedHashTf =
                textFlowDAO.persist(new TextFlow(string, srcLocale));
        }
        String translation =
            microsoftEngine.translate(string, srcLocale, targetLocale);
        TextFlowTarget target = new TextFlowTarget(translation,
            matchedHashTf, targetLocale, provider);
        textFlowTargetDAO.persist(target);
        matchedHashTf.getTargets().add(target);
        return translation;
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
        throws TranslationEngineException {
        if (strings == null || strings.isEmpty() || srcLocale == null
                || targetLocale == null) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>(strings.size());
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
                    results.set(strings.indexOf(string), matchedEntity.getContent());
                }
            } else {
                untranslatedIndexMap.put(string, index);
                indexTextFlowMap.put(index, matchedHashTf);
            }
        }

        // no need of machine translations
        if (untranslatedIndexMap.isEmpty()) {
            return results;
        }

        // fire MT engine search
        List<String> sources = Lists.newArrayList(untranslatedIndexMap.keySet());
        List<String> translations =
            microsoftEngine.translate(sources, srcLocale, targetLocale);

        for (String source: sources) {
            int index = untranslatedIndexMap.get(source);
            String translation = translations.get(sources.indexOf(source));
            results.set(index, translation);

            TextFlow tf = indexTextFlowMap.get(index);
            if (tf == null) {
                tf = textFlowDAO.persist(new TextFlow(source, srcLocale));
            }
            TextFlowTarget target = new TextFlowTarget(translation,
                    tf, targetLocale, provider);
            target = textFlowTargetDAO.persist(target);
            tf.getTargets().add(target);
        }
        return results;
    }

    private Optional<TextFlowTarget> getTargetByProvider(List<TextFlowTarget> targets,
            Provider provider) {
        for (TextFlowTarget target : targets) {
            if (target.getProvider().equals(provider)) {
                return Optional.of(target);
            }
        }
        return Optional.empty();
    }
}
