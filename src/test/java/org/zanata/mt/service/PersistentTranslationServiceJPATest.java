package org.zanata.mt.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.backend.ms.internal.dto.MSLocaleCode;
import org.zanata.mt.dao.TextFlowDAO;
import org.zanata.mt.dao.TextFlowTargetDAO;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.model.TextFlow;
import org.zanata.mt.model.TextFlowTarget;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.backend.ms.MicrosoftTranslatorBackend;
import org.zanata.mt.util.HashUtil;

import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class PersistentTranslationServiceJPATest {

    @Mock
    private TextFlowDAO textFlowDAO;

    @Mock
    private TextFlowTargetDAO textFlowTargetDAO;

    @Mock
    private MicrosoftTranslatorBackend msBackend;

    private PersistentTranslationService persistentTranslationService;

    @Before
    public void setup() {
        persistentTranslationService =
                new PersistentTranslationService(textFlowDAO, textFlowTargetDAO,
                    msBackend);
    }

    @Test
    public void testNewTranslation()
            throws BadRequestException {
        List<String> sources = Lists.newArrayList("string to translate");
        List<AugmentedTranslation> expectedTranslations =
            Lists.newArrayList(new AugmentedTranslation(
                        "translation of:" + sources.get(0), "<MSString>"
                                + "translation of:" + sources.get(0) + "</MSString>"));
        Document doc = new Document();
        Locale sourceLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = new Locale(LocaleId.DE, "German");
        TextFlow expectedTf = new TextFlow(doc, sources.get(0), sourceLocale);
        TextFlowTarget expectedTft =
                new TextFlowTarget(
                        expectedTranslations.get(0).getPlainTranslation(),
                        expectedTranslations.get(0).getRawTranslation(),
                        expectedTf, targetLocale, BackendID.MS);

        String hash = HashUtil.generateHash(sources.get(0));

        when(textFlowDAO.getByContentHash(sourceLocale.getLocaleId(), hash))
                .thenReturn(null);
        when(textFlowDAO.persist(expectedTf)).thenReturn(expectedTf);
        when(textFlowTargetDAO.persist(expectedTft)).thenReturn(expectedTft);

        MSLocaleCode srcLang = new MSLocaleCode(sourceLocale.getLocaleId());
        MSLocaleCode transLang = new MSLocaleCode(targetLocale.getLocaleId());

        when(msBackend.getMappedLocale(sourceLocale.getLocaleId()))
                .thenReturn(srcLang);
        when(msBackend.getMappedLocale(targetLocale.getLocaleId()))
                .thenReturn(transLang);

        when(msBackend.translate(sources, srcLang, transLang,
                MediaType.TEXT_PLAIN_TYPE)).thenReturn(expectedTranslations);

        List<String> translations =
                persistentTranslationService.translate(doc, sources, sourceLocale,
                        targetLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE);

        verify(msBackend).translate(sources, srcLang, transLang,
                MediaType.TEXT_PLAIN_TYPE);
        verify(textFlowDAO).getByContentHash(sourceLocale.getLocaleId(), hash);
        verify(textFlowTargetDAO).persist(expectedTft);
        assertThat(translations).isEqualTo(
                expectedTranslations
                    .stream()
                    .map(AugmentedTranslation::getPlainTranslation)
                    .collect(Collectors.toList()));
    }

    @Test
    public void testTranslationExists() throws BadRequestException {
        List<String> sources = Lists.newArrayList("string to translate");
        String expectedTranslation = "translation of:" + sources.get(0);
        String expectedRawContent =
                "<MSString>" + expectedTranslation + "</MSString>";
        Locale sourceLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = new Locale(LocaleId.DE, "German");

        Document doc = new Document();

        TextFlow expectedTf = new TextFlow(doc, sources.get(0), sourceLocale);
        TextFlowTarget expectedTft = new TextFlowTarget(expectedTranslation,
                expectedRawContent, expectedTf, targetLocale, BackendID.MS);
        expectedTf.getTargets().add(expectedTft);

        String hash = HashUtil.generateHash(sources.get(0));

        when(textFlowDAO.getByContentHash(sourceLocale.getLocaleId(), hash))
                .thenReturn(expectedTf);

        List<String> translations =
                persistentTranslationService
                    .translate(doc, sources, sourceLocale, targetLocale,
                        BackendID.MS, MediaType.TEXT_PLAIN_TYPE);

        verify(textFlowDAO).getByContentHash(sourceLocale.getLocaleId(), hash);
        verify(textFlowTargetDAO).persist(expectedTft);
        assertThat(translations.get(0)).isEqualTo(expectedTranslation);
    }
}
