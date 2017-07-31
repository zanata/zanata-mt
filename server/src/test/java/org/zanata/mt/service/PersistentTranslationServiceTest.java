package org.zanata.mt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.mt.api.APIConstant.AZURE_KEY;
import static org.zanata.mt.backend.mock.MockTranslatorBackend.PREFIX_MOCK_STRING;
import static org.zanata.mt.backend.mock.MockTranslatorBackend.UNICODE_SUPPLEMENTARY;
import static org.zanata.mt.model.BackendID.GOOGLE;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.mt.api.dto.LocaleCode;
import org.zanata.mt.backend.google.GoogleTranslatorBackend;
import org.zanata.mt.backend.mock.MockTranslatorBackend;
import org.zanata.mt.backend.ms.MicrosoftTranslatorBackend;
import org.zanata.mt.backend.ms.internal.dto.MSLocaleCode;
import org.zanata.mt.dao.TextFlowDAO;
import org.zanata.mt.dao.TextFlowTargetDAO;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.TextFlow;
import org.zanata.mt.model.TextFlowTarget;
import org.zanata.mt.util.HashUtil;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class PersistentTranslationServiceTest {

    @Mock
    private TextFlowDAO textFlowDAO;

    @Mock
    private TextFlowTargetDAO textFlowTargetDAO;

    @Mock
    private MicrosoftTranslatorBackend msBackend;

    @Mock
    private GoogleTranslatorBackend googleTranslatorBackend;

    private MockTranslatorBackend mockTranslatorBackend =
            new MockTranslatorBackend();

    private PersistentTranslationService persistentTranslationService;
    @Mock private Instance<TranslatorBackend> translators;

    @Before
    public void setup() {
        when(msBackend.getId()).thenReturn(BackendID.MS);
        when(googleTranslatorBackend.getId()).thenReturn(GOOGLE);
        when(translators.iterator())
                .thenReturn(Lists.newArrayList(googleTranslatorBackend,
                        mockTranslatorBackend, msBackend).iterator());
        persistentTranslationService = new PersistentTranslationService(
                textFlowDAO, textFlowTargetDAO, translators);
    }

    @Test
    public void testEmptyConstructor() {
        persistentTranslationService = new PersistentTranslationService();
    }

    @Test
    public void testValidateEmptySrcLocale() {
        List<String> source = Lists.newArrayList("testing source");
        Locale targetLocale = new Locale(LocaleCode.DE, "German");

        assertThatThrownBy(() -> persistentTranslationService
                .translate(new Document(), source,
                        null, targetLocale,
                        BackendID.MS, MediaType.TEXT_PLAIN_TYPE,
                        Optional.of("tech")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    public void testValidateEmptyTargetLocale() {
        List<String> source = Lists.newArrayList("testing source");
        Locale sourceLocale = new Locale(LocaleCode.EN, "English");

        assertThatThrownBy(
                () -> persistentTranslationService.translate(new Document(),
                        source,
                        sourceLocale, null,
                        BackendID.MS, MediaType.TEXT_PLAIN_TYPE,
                        Optional.of("tech")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    public void testValidateEmptyProvider() {
        List<String> source = Lists.newArrayList("testing source");
        Locale sourceLocale = new Locale(LocaleCode.EN, "English");
        Locale targetLocale = new Locale(LocaleCode.DE, "German");

        assertThatThrownBy(() -> persistentTranslationService
                .translate(new Document(), source,
                        sourceLocale, targetLocale, null,
                        MediaType.TEXT_PLAIN_TYPE, Optional.of("tech")))
                .isInstanceOf(BadRequestException.class);
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
                        BackendID.DEV, MediaType.TEXT_PLAIN_TYPE, Optional.of("tech"));
        assertThat(translations.get(0))
                .contains(source.get(0), PREFIX_MOCK_STRING,
                        UNICODE_SUPPLEMENTARY);
        verify(msBackend).getId();
        verifyNoMoreInteractions(msBackend);
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
                MediaType.TEXT_PLAIN_TYPE, Optional.of("tech"))).thenReturn(expectedTranslations);

        List<String> translations =
                persistentTranslationService.translate(doc, sources, fromLocale,
                        toLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE,
                        Optional.of("tech"));

        verify(msBackend).translate(sources, fromLocaleCode, toLocaleCode,
                MediaType.TEXT_PLAIN_TYPE, Optional.of("tech"));
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
                        MediaType.TEXT_PLAIN_TYPE, Optional.of("tech")))
                .thenReturn(expectedTranslations);

        List<String> translations =
                persistentTranslationService.translate(doc, sources, fromLocale,
                        toLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE,
                        Optional.of("tech"));

        verify(msBackend)
                .translate(sources.subList(0, 1), fromLocaleCode, toLocaleCode,
                        MediaType.TEXT_PLAIN_TYPE, Optional.of("tech"));
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
        when(msBackend.getMappedLocale(toLocale.getLocaleCode()))
                .thenReturn(new MSLocaleCode(toLocale.getLocaleCode()));

        List<String> translations =
                persistentTranslationService
                        .translate(doc, sources, fromLocale, toLocale,
                                BackendID.MS, MediaType.TEXT_PLAIN_TYPE,
                                Optional.of("tech"));

        verify(textFlowDAO).getLatestByContentHash(fromLocale.getLocaleCode(), hash);
        assertThat(translations.get(0)).isEqualTo(expectedTranslation);
    }
}
