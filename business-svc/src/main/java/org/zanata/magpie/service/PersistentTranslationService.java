/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.magpie.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.api.AuthenticatedAccount;
import org.zanata.magpie.backend.BackendLocaleCode;
import org.zanata.magpie.dao.DocumentDAO;
import org.zanata.magpie.dao.TextFlowDAO;
import org.zanata.magpie.dao.TextFlowTargetDAO;
import org.zanata.magpie.event.RequestedMTEvent;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.AugmentedTranslation;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;
import org.zanata.magpie.model.StringType;
import org.zanata.magpie.model.TextFlow;
import org.zanata.magpie.model.TextFlowTarget;
import org.zanata.magpie.util.HashUtil;
import org.zanata.magpie.util.ShortString;

import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class PersistentTranslationService {
    private static final Logger LOG =
        LoggerFactory.getLogger(PersistentTranslationService.class);

    private DocumentDAO documentDAO;
    private TextFlowDAO textFlowDAO;
    private TextFlowTargetDAO textFlowTargetDAO;
    private Event<RequestedMTEvent> requestedMTEvent;
    private AuthenticatedAccount authenticatedAccount;

    private Map<BackendID, TranslatorBackend> translatorBackendMap;

    @SuppressWarnings("unused")
    public PersistentTranslationService() {
    }

    @Inject
    public PersistentTranslationService(DocumentDAO documentDAO,
            TextFlowDAO textFlowDAO,
            TextFlowTargetDAO textFlowTargetDAO,
            Instance<TranslatorBackend> translatorBackends,
            Event<RequestedMTEvent> requestedMTEvent,
            AuthenticatedAccount authenticatedAccount) {
        this.documentDAO = documentDAO;
        this.textFlowDAO = textFlowDAO;
        this.textFlowTargetDAO = textFlowTargetDAO;
        this.requestedMTEvent = requestedMTEvent;
        this.authenticatedAccount = authenticatedAccount;

        Map<BackendID, TranslatorBackend> backendMap = new HashMap<>();
        for (TranslatorBackend backend : translatorBackends) {
            backendMap.put(backend.getId(), backend);
        }

        translatorBackendMap = Collections.unmodifiableMap(backendMap);
    }

    /**
     * Translate multiple string in an api trigger
     *
     * Get from database if exists (hash) from same document,
     * if not exist, get latest TF from DB with matching hash,
     * else from MT engine
     */
    @Transactional
    public List<String> translate(@NotNull Document document,
            @NotNull List<String> sourceStrings,
            @NotNull Locale fromLocale, @NotNull Locale toLocale,
            @NotNull BackendID backendID, @NotNull StringType stringType,
            Optional<String> category)
            throws BadRequestException, MTException {
        // fetch the text flows for later (as part of this new transaction)
        document = documentDAO.reload(document);
        document.getTextFlows();
        if (sourceStrings == null || sourceStrings.isEmpty() || fromLocale == null
                || toLocale == null || backendID == null) {
            throw new BadRequestException();
        }
        if (!authenticatedAccount.hasAuthenticatedAccount()) {
            throw new MTException("not authenticated account trying to trigger MT translation");
        }

        // get translator backend for MT engine by requested backend id
        TranslatorBackend translatorBackend = getTranslatorBackend(backendID);

        BackendLocaleCode mappedFromLocaleCode =
                translatorBackend.getMappedLocale(fromLocale.getLocaleCode());
        BackendLocaleCode mappedToLocaleCode =
                translatorBackend.getMappedLocale(toLocale.getLocaleCode());

        List<String> results = new ArrayList<>(sourceStrings);
        Multimap<String, Integer> untranslatedIndexMap = ArrayListMultimap.create();

        Map<Integer, TextFlow> indexTextFlowMap = Maps.newHashMap();

        // search from database
        int matchCount = 0;
        for (int sourceStringIndex = 0; sourceStringIndex < sourceStrings.size(); sourceStringIndex++) {
            String string = sourceStrings.get(sourceStringIndex);
            String contentHash = HashUtil.generateHash(string);
            Optional<TextFlow> matchedHashTf =
                    tryFindTextFlowByContentHashFromDB(document, fromLocale, toLocale,
                            backendID, contentHash);

            if (matchedHashTf.isPresent()) {
                // we found a matching text flow in database
                // now check to see if it has translation from the same provider
                TextFlow matchedTf = matchedHashTf.get();
                Optional<TextFlowTarget> matchedTarget = findTargetByLocaleAndProvider(
                        toLocale, backendID, matchedTf);

                if (matchedTarget.isPresent()) {
                    TextFlowTarget matchedEntity = matchedTarget.get();
                    matchCount++;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                                "Found match, Source {}:{}:{}\nTranslation {}:{}",
                                fromLocale.getLocaleCode(), document.getUrl(),
                                ShortString.shorten(string),
                                toLocale.getLocaleCode(),
                                ShortString.shorten(matchedEntity.getContent()));
                    }

                    results.set(sourceStringIndex, matchedEntity.getContent());
                } else {
                    untranslatedIndexMap.put(string, sourceStringIndex);
                    indexTextFlowMap.put(sourceStringIndex, matchedTf);
                }
            } else {
                untranslatedIndexMap.put(string, sourceStringIndex);
            }
        }
        LOG.info("found {} of match sources and translations in database", matchCount);

        // see if we got all translations from database records
        if (untranslatedIndexMap.isEmpty()) {
            return results;
        }

        // translate using requested MT engine
        List<String> sourcesToTranslate = new ArrayList<>(untranslatedIndexMap.keySet());
        Date engineInvokeTime = new Date();
        List<AugmentedTranslation> translations =
                translatorBackend.translate(sourcesToTranslate, mappedFromLocaleCode,
                        mappedToLocaleCode, stringType, category);

        LOG.info("triggered MT engine {} from {} to {}", backendID,
                fromLocale.getLocaleCode(), toLocale.getLocaleCode());

        List<String> requestedTextFlows = Lists.newLinkedList();
        long wordCount = 0;
        long charCount = 0;
        for (int i = 0; i < sourcesToTranslate.size(); i++) {
            String source = sourcesToTranslate.get(i);
            AugmentedTranslation translation = translations.get(i);
            // same string may appear several times in a document therefore has several indexes
            Collection<Integer> indexes = untranslatedIndexMap.get(source);
            indexes.forEach(j -> results.set(j, translation.getPlainTranslation()));

            // see if we already have a matched text flow
            // (either in the same document or copied from other document)
            TextFlow tf = indexTextFlowMap.get(indexes.iterator().next());

            try {
                if (tf == null) {
                    tf = createTextFlow(document, source, fromLocale);
                }
                wordCount += tf.getWordCount();
                charCount += tf.getCharCount();
                requestedTextFlows.add(tf.getContentHash());
                TextFlowTarget target =
                        new TextFlowTarget(translation.getPlainTranslation(),
                                translation.getRawTranslation(), tf,
                                toLocale, backendID);
                createOrUpdateTextFlowTarget(target);
            } catch (Exception e) {
                List<Throwable> causalChain = Throwables.getCausalChain(e);
                Optional<Throwable> duplicateKeyEx = causalChain.stream()
                        .filter(t -> t instanceof PSQLException &&
                                t.getMessage().contains(
                                        "ERROR: duplicate key value violates unique constraint"))
                        .findAny();
                if (duplicateKeyEx.isPresent()) {
                    LOG.warn("concurrent requests for document {}", document.getUrl());
                    // we ignore the failed update
                }
            }
        }
        requestedMTEvent.fire(new RequestedMTEvent(document,
                requestedTextFlows, backendID, engineInvokeTime,
                authenticatedAccount.getAuthenticatedAccount().get(), wordCount, charCount));

        return results;
    }

    private @NotNull
    TranslatorBackend getTranslatorBackend(@NotNull BackendID backendID) {
        if (translatorBackendMap.containsKey(backendID)) {
            return translatorBackendMap.get(backendID);
        }
        throw new BadRequestException("Unsupported backendId: " + backendID);
    }

    /**
     * See if the document has the text flow in database already. If not, try to
     * search same content hash from other documents. If any is found, try to
     * copy text flow and text flow target. If none is found, return empty.
     *
     * @param document
     *            current document
     * @param fromLocale
     *            from locale
     * @param toLocale
     *            to locale
     * @param backendID
     *            translation provider
     * @param contentHash
     *            text flow content hash
     * @return optional text flow that has the matching content hash
     */
    private Optional<TextFlow> tryFindTextFlowByContentHashFromDB(
            @NotNull Document document,
            @NotNull Locale fromLocale, @NotNull Locale toLocale,
            @NotNull BackendID backendID, String contentHash) {
        TextFlow matchedHashTf = document.getTextFlows().get(contentHash);
        if (matchedHashTf == null) {
            // we don't have text flow for this document yet,
            // now try to search similar text flow from database
            Optional<TextFlow> tfCopy =
                    tryCopyTextFlowAndTargetFromDB(document, fromLocale,
                            toLocale, contentHash, backendID);

            matchedHashTf = tfCopy.orElse(null);
        }
        return Optional.ofNullable(matchedHashTf);
    }

    public int getMaxLength(@NotNull BackendID backendID) {
        return getTranslatorBackend(backendID).getCharLimitPerRequest();
    }


    /**
     * Find matching contentHash and create a new copy of TextFlow and
     * TextFlowTarget if it is not from the same document. Otherwise, return the
     * same copy. If there is not matching contentHash, return empty.
     *
     * TODO: refactor TextFlow to use pos to allow duplication of content
     */
    private Optional<TextFlow> tryCopyTextFlowAndTargetFromDB(Document document,
            Locale fromLocale, Locale toLocale, String contentHash,
            BackendID backendID) {
        Optional<TextFlow> textFlow =
                textFlowDAO.getLatestByContentHash(fromLocale.getLocaleCode(),
                        contentHash);
        if (textFlow.isPresent()) {
            if (textFlow.get().getDocument().equals(document)) {
                // this document already has this text flow
                return textFlow;
            } else {
                // found a matching text flow from different document
                // copy textFlow and possible target textFlowTarget
                TextFlow newTfCopy =
                        new TextFlow(document, textFlow.get().getContent(),
                                fromLocale);
                Optional<TextFlowTarget> matchedTft =
                        findTargetByLocaleAndProvider(toLocale, backendID,
                                textFlow.get());
                if (matchedTft.isPresent()) {
                    TextFlowTarget tft = matchedTft.get();
                    newTfCopy.getTargets()
                            .add(new TextFlowTarget(tft.getContent(),
                                    tft.getRawContent(), newTfCopy,
                                    toLocale,
                                    backendID));
                }
                newTfCopy = textFlowDAO.persist(newTfCopy);
                document.getTextFlows()
                        .put(newTfCopy.getContentHash(), newTfCopy);
                return Optional.of(newTfCopy);
            }
        }
        return Optional.empty();
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
        Optional<TextFlowTarget> existingTarget = textFlowTargetDAO
                .findTarget(tf, tft.getLocale(), tft.getBackendId());
        if (existingTarget.isPresent()) {
            existingTarget.get()
                    .updateContent(tft.getContent(), tft.getRawContent());
        } else {
            textFlowTargetDAO.persist(tft);
        }
    }

    private static Optional<TextFlowTarget> findTargetByLocaleAndProvider(
            Locale toLocale, BackendID backendID, TextFlow textFlow) {
        return textFlow.getTargets().stream()
                .filter(target -> target.getLocale().equals(toLocale)
                        && target.getBackendId().equals(backendID))
                .findAny();
    }
}
