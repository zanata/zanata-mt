package org.zanata.magpie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.zanata.magpie.model.BackendID.DEV;
import static org.zanata.magpie.model.BackendID.GOOGLE;

import java.util.List;
import java.util.Optional;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.JPATest;
import org.zanata.magpie.api.AuthenticatedAccount;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCode;
import org.zanata.magpie.backend.BackendLocaleCodeImpl;
import org.zanata.magpie.backend.google.GoogleTranslatorBackend;
import org.zanata.magpie.backend.mock.MockTranslatorBackend;
import org.zanata.magpie.backend.ms.MicrosoftTranslatorBackend;
import org.zanata.magpie.dao.TextFlowDAO;
import org.zanata.magpie.dao.TextFlowTargetDAO;
import org.zanata.magpie.event.RequestedMTEvent;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.model.AugmentedTranslation;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;
import org.zanata.magpie.model.StringType;
import org.zanata.magpie.model.TextFlowTarget;
import com.google.common.collect.ImmutableList;

public class PersistentTranslationServiceJpaTest extends JPATest {
    @Mock
    private MicrosoftTranslatorBackend msBackend;
    @Mock
    private GoogleTranslatorBackend googleBackend;
    @Mock
    private MockTranslatorBackend devBackend;
    private PersistentTranslationService service;
    private Document document;
    private Locale fromLocale;
    private Locale toLocale;
    @Mock private Instance<TranslatorBackend> backendInstances;
    @Mock private Event<RequestedMTEvent> requestedMTEvent;

    @Override
    protected void setupTestData() {
        fromLocale = new Locale(LocaleCode.EN, "English");
        toLocale = new Locale(LocaleCode.DE, "Germany");
        document = new Document("http://example.com", fromLocale, toLocale);
        getEm().persist(fromLocale);
        getEm().persist(toLocale);
        getEm().persist(document);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(msBackend.getId()).thenReturn(BackendID.MS);
        when(googleBackend.getId()).thenReturn(GOOGLE);
        when(devBackend.getId()).thenReturn(DEV);
        when(backendInstances.iterator()).thenReturn(
                ImmutableList.of(msBackend, googleBackend, devBackend).iterator());
        AuthenticatedAccount authenticatedAccount = new AuthenticatedAccount();
        authenticatedAccount.setAuthenticatedAccount(new Account());
        service = new PersistentTranslationService(new TextFlowDAO(getEm()),
                new TextFlowTargetDAO(getEm()),
                backendInstances, requestedMTEvent, authenticatedAccount);
    }

    @Test
    public void canTranslateSameFromDifferentProvider() {
        List<String> sourceString = ImmutableList.of("hello");
        Optional<String> category = Optional.empty();
        StringType stringType = StringType.TEXT_PLAIN;

        BackendLocaleCode devFromLocale =
                new BackendLocaleCodeImpl(
                        fromLocale.getLocaleCode());
        BackendLocaleCode devToLocale =
                new BackendLocaleCodeImpl(
                        toLocale.getLocaleCode());
        when(devBackend.getMappedLocale(fromLocale.getLocaleCode()))
                .thenReturn(devFromLocale);
        when(devBackend.getMappedLocale(toLocale.getLocaleCode()))
                .thenReturn(devToLocale);
        when(devBackend.translate(sourceString, devFromLocale,
                devToLocale, stringType, category))
                        .thenReturn(ImmutableList.of(
                                new AugmentedTranslation("hola", "hola")));

        service.translate(document, sourceString, fromLocale, toLocale,
                BackendID.DEV, stringType, category);

        getEm().flush();

        assertThat(getAllTextFlowTargets()).hasSize(1);

        when(msBackend.getMappedLocale(fromLocale.getLocaleCode()))
                .thenReturn(devFromLocale);
        when(msBackend.getMappedLocale(toLocale.getLocaleCode()))
                .thenReturn(devToLocale);
        when(msBackend.translate(sourceString, devFromLocale,
                devToLocale, stringType, category))
                .thenReturn(ImmutableList.of(
                        new AugmentedTranslation("hola", "hola")));

        service.translate(document, sourceString, fromLocale, toLocale,
                BackendID.MS, stringType, category);

        getEm().flush();
        assertThat(getAllTextFlowTargets()).hasSize(2);

    }

    private List<TextFlowTarget> getAllTextFlowTargets() {
        return getEm().createQuery("from TextFlowTarget", TextFlowTarget.class)
                .getResultList();
    }
}
