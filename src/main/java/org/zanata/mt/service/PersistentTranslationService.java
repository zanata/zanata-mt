package org.zanata.mt.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.backend.BackendLocaleCode;
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
import org.zanata.mt.util.ExceptionUtil;
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

    private MicrosoftTranslatorBackend microsoftTranslatorBackend;

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
     * Translate multiple string in an api trigger
     *
     * Get from database if exists (hash), or from MT engine
     */
    @TransactionAttribute
    public List<String> translate(@NotNull Document document,
            @NotNull List<String> strings,
            @NotNull Locale srcLocale, @NotNull Locale targetLocale,
            @NotNull BackendID backendID, @NotNull MediaType mediaType)
            throws BadRequestException, ZanataMTException {
        if (strings == null || strings.isEmpty() || srcLocale == null
                || targetLocale == null || backendID == null) {
            throw new BadRequestException();
        }

        List<String> results = new ArrayList<>(strings);
        Map<String, Integer> untranslatedIndexMap = Maps.newHashMap();
        Map<Integer, TextFlow> indexTextFlowMap = Maps.newHashMap();

        // search from database
        for (int index = 0; index < strings.size(); index++) {
            String string = strings.get(index);
            String hash = HashUtil.generateHash(string);
            TextFlow matchedHashTf =
                    textFlowDAO.getByContentHash(srcLocale.getLocaleId(), hash);

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

        // all translations got from database records
        if (untranslatedIndexMap.isEmpty()) {
            return results;
        }

        // trigger MT engine search
        List<String> sources = Lists.newArrayList(untranslatedIndexMap.keySet());

        BackendLocaleCode mappedSrcLang =
                getMappedLocale(srcLocale.getLocaleId());
        BackendLocaleCode mappedTransLang =
                getMappedLocale(targetLocale.getLocaleId());

        List<AugmentedTranslation> translations =
            microsoftTranslatorBackend
                .translate(sources, mappedSrcLang, mappedTransLang, mediaType);

        for (String source: sources) {
            int index = untranslatedIndexMap.get(source);
            AugmentedTranslation translation = translations.get(sources.indexOf(source));
            results.set(index, translation.getPlainTranslation());

            TextFlow tf = indexTextFlowMap.get(index);
            if (tf == null) {
                tf = createOrFetchTextFlow(document, source, srcLocale);
            }
            TextFlowTarget target =
                    new TextFlowTarget(translation.getPlainTranslation(),
                            translation.getRawTranslation(), tf,
                            targetLocale, backendID);
            createOrUpdateTextFlowTarget(target);
        }
        return results;
    }

    /**
     * This is to handle concurrent db request for 2 same text flow is being
     * persisted at the same time.
     */
    private TextFlow createOrFetchTextFlow(Document document, String source,
            Locale locale) {
        TextFlow tf = new TextFlow(document, source, locale);
        try {
            tf = textFlowDAO.persist(tf);
        } catch (Exception e) {
            if (ExceptionUtil.isConstraintViolationException(e)) {
                tf = textFlowDAO
                        .getByContentHash(locale.getLocaleId(),
                                tf.getContentHash());
            }
        } finally {
            return tf;
        }
    }

    /**
     * If found matching TextFlowTarget (locale + backendId),
     * update the content and rawContent, else create new TextFlowTarget
     */
    private void createOrUpdateTextFlowTarget(TextFlowTarget tft) {
        TextFlow tf = tft.getTextFlow();
        List<TextFlowTarget> existingTfts =
                tf.getTargetsByLocaleId(tft.getLocale().getLocaleId());
        if (existingTfts.isEmpty()) {
            textFlowTargetDAO.persist(tft);
            tf.getTargets().add(tft);
        } else {
            Optional<TextFlowTarget> existingTft =
                    getTargetByProvider(existingTfts, tft.getBackendId());
            if (existingTft.isPresent()) {
                existingTft.get()
                        .updateContent(tft.getContent(), tft.getRawContent());
                textFlowTargetDAO.persist(existingTft.get());
            }
        }
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

    public BackendLocaleCode getMappedLocale(@Nonnull LocaleId localeId) {
        return microsoftTranslatorBackend.getMappedLocale(localeId);
    }
}
