package org.zanata.mt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.when;
import static org.zanata.mt.model.BackendID.DEV;
import static org.zanata.mt.model.BackendID.GOOGLE;

import java.util.List;
import java.util.Optional;

import javax.enterprise.inject.Instance;
import javax.ws.rs.core.MediaType;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.mt.JPATest;
import org.zanata.mt.api.dto.LocaleCode;
import org.zanata.mt.backend.google.GoogleTranslatorBackend;
import org.zanata.mt.backend.mock.MockTranslatorBackend;
import org.zanata.mt.backend.ms.MicrosoftTranslatorBackend;
import org.zanata.mt.dao.TextFlowDAO;
import org.zanata.mt.dao.TextFlowTargetDAO;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.TextFlowTarget;

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
                newArrayList(msBackend, googleBackend, devBackend).iterator());
        service = new PersistentTranslationService(new TextFlowDAO(getEm()),
                new TextFlowTargetDAO(getEm()),
                backendInstances);
    }

    @Test
    public void canTranslateSameFromDifferentProvider() {
        List<String> sourceString = newArrayList("hello");
        Optional<String> category = Optional.empty();
        MediaType mediaType = MediaType.TEXT_PLAIN_TYPE;

        MockTranslatorBackend.MockLocaleCode devFromLocale =
                new MockTranslatorBackend.MockLocaleCode(
                        fromLocale.getLocaleCode());
        MockTranslatorBackend.MockLocaleCode devToLocale =
                new MockTranslatorBackend.MockLocaleCode(
                        toLocale.getLocaleCode());
        when(devBackend.getMappedLocale(fromLocale.getLocaleCode()))
                .thenReturn(Optional.of(devFromLocale));
        when(devBackend.getMappedLocale(toLocale.getLocaleCode()))
                .thenReturn(Optional.of(devToLocale));
        when(devBackend.translate(sourceString, Optional.of(devFromLocale),
                devToLocale, mediaType, category))
                        .thenReturn(newArrayList(
                                new AugmentedTranslation("hola", "hola")));

        service.translate(document, sourceString, fromLocale, toLocale,
                BackendID.DEV, mediaType, category);

        getEm().flush();

        assertThat(getAllTextFlowTargets()).hasSize(1);

        when(msBackend.getMappedLocale(fromLocale.getLocaleCode()))
                .thenReturn(Optional.of(devFromLocale));
        when(msBackend.getMappedLocale(toLocale.getLocaleCode()))
                .thenReturn(Optional.of(devToLocale));
        when(msBackend.translate(sourceString, Optional.of(devFromLocale),
                devToLocale, mediaType, category))
                .thenReturn(newArrayList(
                        new AugmentedTranslation("hola", "hola")));

        service.translate(document, sourceString, fromLocale, toLocale,
                BackendID.MS, mediaType, category);

        getEm().flush();
        assertThat(getAllTextFlowTargets()).hasSize(2);

    }

    private List<TextFlowTarget> getAllTextFlowTargets() {
        return getEm().createQuery("from TextFlowTarget", TextFlowTarget.class)
                .getResultList();
    }
}
