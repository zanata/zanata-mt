package org.zanata.mt.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.mt.api.dto.LocaleCode;
import org.zanata.mt.backend.mock.MockTranslatorBackend;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
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

    private MockTranslatorBackend mockTranslatorBackend =
            new MockTranslatorBackend();

    private PersistentTranslationService persistentTranslationService;

    @Before
    public void setup() {
        persistentTranslationService =
                new PersistentTranslationService(textFlowDAO, textFlowTargetDAO,
                        msBackend, mockTranslatorBackend);
    }

    @Test
    public void testDevMode() {
        List<String> source = Lists.newArrayList("testing source");
        Locale fromLocale = new Locale(LocaleCode.EN, "English");
        Locale toLocale = new Locale(LocaleCode.DE, "German");
        Document doc = new Document();
        TextFlow expectedTf = new TextFlow(doc, source.get(0), fromLocale);
        TextFlowTarget expectedTft =
                new TextFlowTarget(source.get(0), source.get(0), expectedTf,
                        toLocale, BackendID.DEV);

        String hash = HashUtil.generateHash(source.get(0));

        when(textFlowDAO.getLatestByContentHash(fromLocale.getLocaleCode(), hash))
                .thenReturn(Optional.empty());
        when(textFlowDAO.persist(expectedTf)).thenReturn(expectedTf);
        when(textFlowTargetDAO.persist(expectedTft)).thenReturn(expectedTft);

        List<String> translations = persistentTranslationService
                .translate(new Document(), source, fromLocale, toLocale,
                        BackendID.DEV, MediaType.TEXT_PLAIN_TYPE);
        assertThat(translations.get(0))
                .isEqualTo(MockTranslatorBackend.PREFIX_MOCK_STRING +
                        source.get(0));
        verifyZeroInteractions(msBackend);
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
        Locale fromLocale = new Locale(LocaleCode.EN, "English");
        Locale toLocale = new Locale(LocaleCode.DE, "German");
        TextFlow expectedTf = new TextFlow(doc, sources.get(0), fromLocale);
        TextFlowTarget expectedTft =
                new TextFlowTarget(
                        expectedTranslations.get(0).getPlainTranslation(),
                        expectedTranslations.get(0).getRawTranslation(),
                        expectedTf, toLocale, BackendID.MS);

        String hash = HashUtil.generateHash(sources.get(0));

        when(textFlowDAO.getLatestByContentHash(fromLocale.getLocaleCode(), hash))
                .thenReturn(Optional.empty());
        when(textFlowDAO.persist(expectedTf)).thenReturn(expectedTf);
        when(textFlowTargetDAO.persist(expectedTft)).thenReturn(expectedTft);

        MSLocaleCode fromLocaleCode = new MSLocaleCode(fromLocale.getLocaleCode());
        MSLocaleCode toLocaleCode = new MSLocaleCode(toLocale.getLocaleCode());

        when(msBackend.getMappedLocale(fromLocale.getLocaleCode()))
                .thenReturn(fromLocaleCode);
        when(msBackend.getMappedLocale(toLocale.getLocaleCode()))
                .thenReturn(toLocaleCode);

        when(msBackend.translate(sources, fromLocaleCode, toLocaleCode,
                MediaType.TEXT_PLAIN_TYPE)).thenReturn(expectedTranslations);

        List<String> translations =
                persistentTranslationService.translate(doc, sources, fromLocale,
                        toLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE);

        verify(msBackend).translate(sources, fromLocaleCode, toLocaleCode,
                MediaType.TEXT_PLAIN_TYPE);
        verify(textFlowDAO).getLatestByContentHash(fromLocale.getLocaleCode(), hash);
        verify(textFlowTargetDAO).persist(expectedTft);
        assertThat(translations).isEqualTo(
                expectedTranslations
                    .stream()
                    .map(AugmentedTranslation::getPlainTranslation)
                    .collect(Collectors.toList()));
    }

    @Test
    public void testNewTranslationDuplicateString()
            throws BadRequestException {
        List<String> sources = Lists.newArrayList("string to translate", "string to translate");
        List<AugmentedTranslation> expectedTranslations =
                Lists.newArrayList(new AugmentedTranslation(
                        "translation of:" + sources.get(0), "<MSString>"
                        + "translation of:" + sources.get(0) + "</MSString>"), new AugmentedTranslation(
                        "translation of:" + sources.get(0), "<MSString>"
                        + "translation of:" + sources.get(0) + "</MSString>"));
        Document doc = new Document();
        Locale fromLocale = new Locale(LocaleCode.EN, "English");
        Locale toLocale = new Locale(LocaleCode.DE, "German");
        TextFlow expectedTf = new TextFlow(doc, sources.get(0), fromLocale);
        TextFlowTarget expectedTft =
                new TextFlowTarget(
                        expectedTranslations.get(0).getPlainTranslation(),
                        expectedTranslations.get(0).getRawTranslation(),
                        expectedTf, toLocale, BackendID.MS);

        String hash = HashUtil.generateHash(sources.get(0));

        when(textFlowDAO.getLatestByContentHash(fromLocale.getLocaleCode(), hash))
                .thenReturn(Optional.empty());
        when(textFlowDAO.persist(expectedTf)).thenReturn(expectedTf);
        when(textFlowTargetDAO.persist(expectedTft)).thenReturn(expectedTft);

        MSLocaleCode fromLocaleCode = new MSLocaleCode(fromLocale.getLocaleCode());
        MSLocaleCode toLocaleCode = new MSLocaleCode(toLocale.getLocaleCode());

        when(msBackend.getMappedLocale(fromLocale.getLocaleCode()))
                .thenReturn(fromLocaleCode);
        when(msBackend.getMappedLocale(toLocale.getLocaleCode()))
                .thenReturn(toLocaleCode);

        when(msBackend
                .translate(sources.subList(0, 1), fromLocaleCode, toLocaleCode,
                        MediaType.TEXT_PLAIN_TYPE))
                .thenReturn(expectedTranslations);

        List<String> translations =
                persistentTranslationService.translate(doc, sources, fromLocale,
                        toLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE);

        verify(msBackend)
                .translate(sources.subList(0, 1), fromLocaleCode, toLocaleCode,
                        MediaType.TEXT_PLAIN_TYPE);
        verify(textFlowDAO, times(2)).getLatestByContentHash(fromLocale.getLocaleCode(), hash);
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
        Locale fromLocale = new Locale(LocaleCode.EN, "English");
        Locale toLocale = new Locale(LocaleCode.DE, "German");

        Document doc = new Document();

        TextFlow expectedTf = new TextFlow(doc, sources.get(0), fromLocale);
        TextFlowTarget expectedTft = new TextFlowTarget(expectedTranslation,
                expectedRawContent, expectedTf, toLocale, BackendID.MS);
        expectedTf.getTargets().add(expectedTft);

        String hash = HashUtil.generateHash(sources.get(0));

        when(textFlowDAO.getLatestByContentHash(fromLocale.getLocaleCode(), hash))
                .thenReturn(Optional.of(expectedTf));

        List<String> translations =
                persistentTranslationService
                    .translate(doc, sources, fromLocale, toLocale,
                        BackendID.MS, MediaType.TEXT_PLAIN_TYPE);

        verify(textFlowDAO).getLatestByContentHash(fromLocale.getLocaleCode(), hash);
        assertThat(translations.get(0)).isEqualTo(expectedTranslation);
    }
}
