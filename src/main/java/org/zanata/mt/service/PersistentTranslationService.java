package org.zanata.mt.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.dao.TextFlowDAO;
import org.zanata.mt.dao.TextFlowTargetDAO;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.model.TextFlow;
import org.zanata.mt.model.TextFlowTarget;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.backend.ms.MicrosoftTranslatorBackend;
import org.zanata.mt.util.HashUtil;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class PersistentTranslationService {
    private static final Logger LOG =
        LoggerFactory.getLogger(PersistentTranslationService.class);

    private TextFlowDAO textFlowDAO;

    private TextFlowTargetDAO textFlowTargetDAO;

    private MicrosoftTranslatorBackend microsoftTranslatorBackend;

    // Max length for single string in Microsoft Engine
    public static final int MAX_LENGTH = 6000;

    // Max length before logging warning
    public static final int MAX_LENGTH_WARN = 3000;

    @SuppressWarnings("unused")
    public PersistentTranslationService() {
    }

    @Inject
    public PersistentTranslationService(TextFlowDAO textFlowDAO,
        TextFlowTargetDAO textFlowTargetDAO,
        MicrosoftTranslatorBackend microsoftTranslatorBackend) {
        this.textFlowDAO = textFlowDAO;
        this.textFlowTargetDAO = textFlowTargetDAO;
        this.microsoftTranslatorBackend = microsoftTranslatorBackend;
    }

    /**
     * Translate single string in an api trigger
     *
     * Get from database if exists (hash), or from MT engine
     */
    @TransactionAttribute
    public String translate(@NotNull @Size(max = MAX_LENGTH) String string,
            @NotNull Locale srcLocale, @NotNull Locale targetLocale,
            @NotNull BackendID backendID, @NotNull MediaType mediaType)
            throws BadRequestException, ZanataMTException {
        List<String> translations = translate(Lists.newArrayList(string),
                srcLocale, targetLocale, backendID, mediaType);
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
            @NotNull BackendID backendID, @NotNull MediaType mediaType)
            throws BadRequestException, ZanataMTException {
        if (strings == null || strings.isEmpty() || srcLocale == null
                || targetLocale == null || backendID == null) {
            throw new BadRequestException();
        }
        int totalChar = strings.stream().mapToInt(String::length).sum();

        /**
         * return original string if it is more than MAX_LENGTH
         */
        if (totalChar > MAX_LENGTH) {
            LOG.warn("Requested string length is more than " + MAX_LENGTH);
            return strings;
        }
        if (totalChar > MAX_LENGTH_WARN) {
            LOG.warn("Requested string length is more than " + MAX_LENGTH_WARN);
        }

        List<String> results = new ArrayList<>(strings);
        Map<String, Integer> untranslatedIndexMap = Maps.newHashMap();
        Map<Integer, TextFlow> indexTextFlowMap = Maps.newHashMap();

        // search from database
        for (String string: strings) {
            String hash =
                    HashUtil.generateHash(string, srcLocale.getLocaleId());
            TextFlow matchedHashTf = textFlowDAO.getByHash(hash);

            int index = strings.indexOf(string);

            if (matchedHashTf != null) {
                Optional<TextFlowTarget> matchedTarget = getTargetByProvider(
                        matchedHashTf.getTargetsByLocaleId(
                                targetLocale.getLocaleId()), backendID);

                if (matchedTarget.isPresent()) {
                    TextFlowTarget matchedEntity = matchedTarget.get();
                    matchedEntity.incrementCount();
                    textFlowTargetDAO.persist(matchedEntity);
                    LOG.info(
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
        List<AugmentedTranslation> translations =
            microsoftTranslatorBackend
                .translate(sources, srcLocale, targetLocale, mediaType);

        for (String source: sources) {
            int index = untranslatedIndexMap.get(source);
            AugmentedTranslation translation = translations.get(sources.indexOf(source));
            results.set(index, translation.getPlainTranslation());

            TextFlow tf = indexTextFlowMap.get(index);
            if (tf == null) {
                tf = textFlowDAO.persist(new TextFlow(source, srcLocale));
            }
            TextFlowTarget target =
                    new TextFlowTarget(translation.getPlainTranslation(),
                            translation.getRawTranslation(), tf,
                            targetLocale, backendID);
            target = textFlowTargetDAO.persist(target);
            tf.getTargets().add(target);
        }
        return results;
    }

    private Optional<TextFlowTarget> getTargetByProvider(
            List<TextFlowTarget> targets, BackendID backendID) {
        for (TextFlowTarget target : targets) {
            if (target.getBackendId().equals(backendID)) {
                return Optional.of(target);
            }
        }
        return Optional.empty();
    }
}
