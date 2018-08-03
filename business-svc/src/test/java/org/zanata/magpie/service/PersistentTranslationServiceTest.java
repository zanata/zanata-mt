package org.zanata.magpie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.magpie.model.BackendID.DEV;
import static org.zanata.magpie.model.BackendID.GOOGLE;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.ws.rs.BadRequestException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.magpie.api.AuthenticatedAccount;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCode;
import org.zanata.magpie.backend.BackendLocaleCodeImpl;
import org.zanata.magpie.dao.TextFlowDAO;
import org.zanata.magpie.dao.TextFlowTargetDAO;
import org.zanata.magpie.event.RequestedMTEvent;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.model.AugmentedTranslation;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;
import org.zanata.magpie.model.StringType;
import org.zanata.magpie.model.TextFlow;
import org.zanata.magpie.model.TextFlowTarget;
import org.zanata.magpie.util.HashUtil;

import com.google.common.collect.ImmutableList;

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
    private TranslatorBackend msBackend;

    @Mock
    private TranslatorBackend googleTranslatorBackend;

    @Mock
    private TranslatorBackend mockTranslatorBackend;

    private PersistentTranslationService persistentTranslationService;
    @Mock private Instance<TranslatorBackend> translators;
    @Mock private Event<RequestedMTEvent> requestedMTEvent;
    private AuthenticatedAccount authenticatedAccount;

    @Before
    public void setup() {
        when(msBackend.getId()).thenReturn(BackendID.MS);
        when(googleTranslatorBackend.getId()).thenReturn(GOOGLE);
        when(mockTranslatorBackend.getId()).thenReturn(DEV);
        when(translators.iterator())
                .thenReturn(ImmutableList.of(googleTranslatorBackend,
                        mockTranslatorBackend, msBackend).iterator());
        authenticatedAccount = new AuthenticatedAccount();
        authenticatedAccount.setAuthenticatedAccount(new Account());
        persistentTranslationService = new PersistentTranslationService(
                textFlowDAO, textFlowTargetDAO, translators, requestedMTEvent,
                authenticatedAccount);
    }

    @Test
    public void testEmptyConstructor() {
        persistentTranslationService = new PersistentTranslationService();
    }

    @Test
    public void willThrowExceptionIfNoAuthenticatedAccount() {
        authenticatedAccount.setAuthenticatedAccount(null);
        List<String> source = ImmutableList.of("testing source");
        Locale targetLocale = new Locale(LocaleCode.DE, "German");
        assertThatThrownBy(() -> persistentTranslationService.translate(new Document(), source,
                new Locale(LocaleCode.EN_US, "English"), targetLocale,
                BackendID.MS, StringType.TEXT_PLAIN,
                Optional.of("tech"))).isInstanceOf(MTException.class);
    }

    @Test
    public void testValidateEmptySrcLocale() {
        List<String> source = ImmutableList.of("testing source");
        Locale targetLocale = new Locale(LocaleCode.DE, "German");

        assertThatThrownBy(() -> persistentTranslationService
                .translate(new Document(), source,
                        null, targetLocale,
                        BackendID.MS, StringType.TEXT_PLAIN,
                        Optional.of("tech")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    public void testValidateEmptyTargetLocale() {
        List<String> source = ImmutableList.of("testing source");
        Locale sourceLocale = new Locale(LocaleCode.EN, "English");

        assertThatThrownBy(
                () -> persistentTranslationService.translate(new Document(),
                        source,
                        sourceLocale, null,
                        BackendID.MS, StringType.TEXT_PLAIN,
                        Optional.of("tech")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    public void testValidateEmptyProvider() {
        List<String> source = ImmutableList.of("testing source");
        Locale sourceLocale = new Locale(LocaleCode.EN, "English");
        Locale targetLocale = new Locale(LocaleCode.DE, "German");

        assertThatThrownBy(() -> persistentTranslationService
                .translate(new Document(), source,
                        sourceLocale, targetLocale, null,
                        StringType.TEXT_PLAIN, Optional.of("tech")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    public void testNewTranslation()
            throws BadRequestException {
        List<String> sources = ImmutableList.of("string to translate");
        List<AugmentedTranslation> expectedTranslations =
                ImmutableList.of(new AugmentedTranslation(
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

        BackendLocaleCode fromLocaleCode = new BackendLocaleCodeImpl(fromLocale.getLocaleCode());
        BackendLocaleCode toLocaleCode = new BackendLocaleCodeImpl(toLocale.getLocaleCode());

        when(msBackend.getMappedLocale(fromLocale.getLocaleCode()))
                .thenReturn(fromLocaleCode);
        when(msBackend.getMappedLocale(toLocale.getLocaleCode()))
                .thenReturn(toLocaleCode);

        when(msBackend.translate(sources, fromLocaleCode, toLocaleCode,
                StringType.TEXT_PLAIN, Optional.of("tech"))).thenReturn(expectedTranslations);

        List<String> translations =
                persistentTranslationService.translate(doc, sources, fromLocale,
                        toLocale, BackendID.MS, StringType.TEXT_PLAIN,
                        Optional.of("tech"));

        verify(msBackend).translate(sources, fromLocaleCode, toLocaleCode,
                StringType.TEXT_PLAIN, Optional.of("tech"));
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
        List<String> sources = ImmutableList.of("string to translate", "string to translate");
        List<AugmentedTranslation> expectedTranslations =
                ImmutableList.of(new AugmentedTranslation(
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

        BackendLocaleCode fromLocaleCode = new BackendLocaleCodeImpl(fromLocale.getLocaleCode());
        BackendLocaleCode toLocaleCode = new BackendLocaleCodeImpl(toLocale.getLocaleCode());

        when(msBackend.getMappedLocale(fromLocale.getLocaleCode()))
                .thenReturn(fromLocaleCode);
        when(msBackend.getMappedLocale(toLocale.getLocaleCode()))
                .thenReturn(toLocaleCode);

        when(msBackend
                .translate(sources.subList(0, 1), fromLocaleCode, toLocaleCode,
                        StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(expectedTranslations);

        List<String> translations =
                persistentTranslationService.translate(doc, sources, fromLocale,
                        toLocale, BackendID.MS, StringType.TEXT_PLAIN,
                        Optional.of("tech"));

        verify(msBackend)
                .translate(sources.subList(0, 1), fromLocaleCode, toLocaleCode,
                        StringType.TEXT_PLAIN, Optional.of("tech"));
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
        List<String> sources = ImmutableList.of("string to translate");
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
                .thenReturn(new BackendLocaleCodeImpl(toLocale.getLocaleCode()));

        List<String> translations =
                persistentTranslationService
                        .translate(doc, sources, fromLocale, toLocale,
                                BackendID.MS, StringType.TEXT_PLAIN,
                                Optional.of("tech"));

        verify(textFlowDAO).getLatestByContentHash(fromLocale.getLocaleCode(), hash);
        assertThat(translations.get(0)).isEqualTo(expectedTranslation);
    }
}
