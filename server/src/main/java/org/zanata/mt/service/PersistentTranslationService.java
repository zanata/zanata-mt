package org.zanata.mt.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;

import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.backend.BackendLocaleCode;
import org.zanata.mt.backend.google.GoogleTranslatorBackend;
import org.zanata.mt.backend.mock.MockTranslatorBackend;
import org.zanata.mt.dao.TextFlowDAO;
import org.zanata.mt.dao.TextFlowTargetDAO;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.Document;
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
@Stateless
public class PersistentTranslationService {
    private static final Logger LOG =
        LoggerFactory.getLogger(PersistentTranslationService.class);

    private TextFlowDAO textFlowDAO;

    private TextFlowTargetDAO textFlowTargetDAO;

    private Map<BackendID, TranslatorBackend> translatorBackendMap;

    @SuppressWarnings("unused")
    public PersistentTranslationService() {
    }

    @Inject
    public PersistentTranslationService(TextFlowDAO textFlowDAO,
        TextFlowTargetDAO textFlowTargetDAO,
            MicrosoftTranslatorBackend microsoftTranslatorBackend,
            GoogleTranslatorBackend googleTranslatorBackend,
            MockTranslatorBackend mockTranslatorBackend) {
        this.textFlowDAO = textFlowDAO;
        this.textFlowTargetDAO = textFlowTargetDAO;

        Map<BackendID, TranslatorBackend> backendMap = new HashMap<>();
        backendMap.put(BackendID.MS, microsoftTranslatorBackend);
        backendMap.put(BackendID.GOOGLE, googleTranslatorBackend);
        backendMap.put(BackendID.DEV, mockTranslatorBackend);

        translatorBackendMap = Collections.unmodifiableMap(backendMap);
    }

    /**
     * Translate multiple string in an api trigger
     *
     * Get from database if exists (hash) from same document,
     * if not exist, get latest TF from DB with matching hash,
     * else from MT engine
     */
    @TransactionAttribute
    public List<String> translate(@NotNull Document document,
            @NotNull List<String> strings,
            @NotNull Locale fromLocale, @NotNull Locale toLocale,
            @NotNull BackendID backendID, @NotNull MediaType mediaType,
            Optional<String> category)
            throws BadRequestException, ZanataMTException {
        if (strings == null || strings.isEmpty() || fromLocale == null
                || toLocale == null || backendID == null) {
            throw new BadRequestException();
        }

        List<String> results = new ArrayList<>(strings);
        Multimap<String, Integer> untranslatedIndexMap = ArrayListMultimap.create();

        Map<Integer, TextFlow> indexTextFlowMap = Maps.newHashMap();

        // search from database
        for (int index = 0; index < strings.size(); index++) {
            String string = strings.get(index);
            String contentHash = HashUtil.generateHash(string);
            TextFlow matchedHashTf = document.getTextFlows().get(contentHash);
            if (matchedHashTf == null) {
                Optional<TextFlow> tfCopy =
                        copyTextFlowAndTargetFromDB(document, fromLocale,
                                toLocale, contentHash, backendID);

                matchedHashTf = tfCopy.isPresent() ? tfCopy.get() : null;
            }

            if (matchedHashTf != null) {
                Optional<TextFlowTarget> matchedTarget = filterTargetByProvider(
                        matchedHashTf.getTargetsByLocaleCode(
                                toLocale.getLocaleCode()), backendID);

                if (matchedTarget.isPresent()) {
                    TextFlowTarget matchedEntity = matchedTarget.get();
                    LOG.info(
                            "Found match, Source {}:{}:{}\nTranslation {}:{}",
                            fromLocale.getLocaleCode(), document.getUrl(),
                            string,
                            toLocale.getLocaleCode(),
                            matchedEntity.getContent());

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

        // all translations got from database records
        if (untranslatedIndexMap.isEmpty()) {
            return results;
        }

        // trigger MT engine search
        TranslatorBackend translatorBackend = getTranslatorBackend(backendID);

        List<String> sources = Lists.newArrayList(untranslatedIndexMap.keySet());

        Optional<BackendLocaleCode> mappedFromLocaleCode =
                translatorBackend.getMappedLocale(fromLocale.getLocaleCode());
        Optional<BackendLocaleCode> mappedToLocaleCode =
                translatorBackend.getMappedLocale(toLocale.getLocaleCode());

        if (!mappedToLocaleCode.isPresent()) {
            throw new BadRequestException("can not map " + toLocale + " to provider " + backendID + " locale");
        }

        List<AugmentedTranslation> translations =
                translatorBackend.translate(sources, mappedFromLocaleCode,
                        mappedToLocaleCode.get(), mediaType, category);

        for (String source: sources) {
            Collection<Integer> indexes = untranslatedIndexMap.get(source);
            AugmentedTranslation translation = translations.get(sources.indexOf(source));
            for (int index: indexes) {
                results.set(index, translation.getPlainTranslation());
            }

            TextFlow tf = indexTextFlowMap.get(indexes.iterator().next());
            if (tf == null) {
                tf = createTextFlow(document, source, fromLocale);
            }
            TextFlowTarget target =
                    new TextFlowTarget(translation.getPlainTranslation(),
                            translation.getRawTranslation(), tf,
                            toLocale, backendID);
            createOrUpdateTextFlowTarget(target);
        }
        return results;
    }

    public int getMaxLength(@NotNull BackendID backendID) {
        return getTranslatorBackend(backendID).getCharLimitPerRequest();
    }


    private @NotNull
    TranslatorBackend getTranslatorBackend(@NotNull BackendID backendID) {
        if (translatorBackendMap.containsKey(backendID)) {
            return translatorBackendMap.get(backendID);
        }
        throw new BadRequestException("Unsupported backendId:" + backendID);
    }

    /**
     * Find matching contentHash and create a new copy of TextFlow and
     * TextFlowTarget if it is not from the same document. Otherwise, return the
     * same copy.
     *
     * TODO: refactor TextFlow to use pos to allow duplication of content
     */
    private Optional<TextFlow> copyTextFlowAndTargetFromDB(Document document,
            Locale fromLocale, Locale toLocale, String contentHash,
            BackendID backendID) {
        Optional<TextFlow> textFlow =
                textFlowDAO.getLatestByContentHash(fromLocale.getLocaleCode(),
                        contentHash);
        Optional<TextFlow> copy = Optional.empty();
        if (textFlow.isPresent()) {
            if (textFlow.get().getDocument().equals(document)) {
                copy = textFlow;
            } else {
                // copy textFlow and target textFlowTarget
                List<TextFlowTarget> tfts =
                        textFlow.get().getTargetsByLocaleCode(toLocale.getLocaleCode());
                TextFlow newTfCopy =
                        new TextFlow(document, textFlow.get().getContent(),
                                fromLocale);
                if (!tfts.isEmpty()) {
                    Optional<TextFlowTarget> tft =
                            filterTargetByProvider(tfts, backendID);
                    if (tft.isPresent()) {
                        newTfCopy.getTargets()
                                .add(new TextFlowTarget(tft.get().getContent(),
                                        tft.get().getRawContent(), newTfCopy,
                                        toLocale,
                                        tft.get().getBackendId()));
                    }
                }
                newTfCopy = textFlowDAO.persist(newTfCopy);
                document.getTextFlows()
                        .put(newTfCopy.getContentHash(), newTfCopy);
                return Optional.of(newTfCopy);
            }
        }
        return copy;
    }

    private TextFlow createTextFlow(Document document, String source,
            Locale locale) {
        TextFlow tf = new TextFlow(document, source, locale);
        tf = textFlowDAO.persist(tf);
        return tf;
    }

    /**
     * If found matching TextFlowTarget (locale + backendId),
     * update the content and rawContent, else create new TextFlowTarget
     */
    private void createOrUpdateTextFlowTarget(TextFlowTarget tft) {
        TextFlow tf = tft.getTextFlow();
        List<TextFlowTarget> existingTfts =
                tf.getTargetsByLocaleCode(tft.getLocale().getLocaleCode());
        if (existingTfts.isEmpty()) {
            textFlowTargetDAO.persist(tft);
            tf.getTargets().add(tft);
        } else {
            Optional<TextFlowTarget> existingTft =
                    filterTargetByProvider(existingTfts, tft.getBackendId());
            if (existingTft.isPresent()) {
                existingTft.get()
                        .updateContent(tft.getContent(), tft.getRawContent());
                textFlowTargetDAO.persist(existingTft.get());
            } else {
                textFlowTargetDAO.persist(tft);
            }
        }
    }

    private Optional<TextFlowTarget> filterTargetByProvider(
            List<TextFlowTarget> targets, BackendID backendID) {
        for (TextFlowTarget target : targets) {
            if (target.getBackendId().equals(backendID)) {
                return Optional.of(target);
            }
        }
        return Optional.empty();
    }
}
