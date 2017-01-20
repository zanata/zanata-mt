package org.zanata.mt.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.dao.TextFlowDAO;
import org.zanata.mt.dao.TextFlowTargetDAO;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.model.TextFlow;
import org.zanata.mt.model.TextFlowTarget;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.backend.ms.MicrosoftTranslatorBackend;
import org.zanata.mt.util.HashUtil;

import com.google.common.collect.Lists;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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
    private MicrosoftTranslatorBackend msProvider;

    private PersistentTranslationService persistentTranslationService;

    @Before
    public void setup() {
        persistentTranslationService =
                new PersistentTranslationService(textFlowDAO, textFlowTargetDAO,
                    msProvider);
    }

    @Test
    public void testNewTranslation()
            throws BadRequestException {
        List<String> sources = Lists.newArrayList("string to translate");
        List<AugmentedTranslation> expectedTranslations =
                Lists.newArrayList(new AugmentedTranslation(
                        "translation of:" + sources.get(0), "<MSString>"
                                + "translation of:" + sources.get(0) + "</MSString>"));
       
        Locale sourceLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = new Locale(LocaleId.DE, "German");
        TextFlow expectedTf = new TextFlow(sources.get(0), sourceLocale);
        TextFlowTarget expectedTft =
                new TextFlowTarget(expectedTranslations.get(0).getPlainTranslation(),
                        expectedTranslations.get(0).getRawTranslation(), expectedTf,
                        targetLocale, BackendID.MS);

        String hash = HashUtil.generateHash(sources.get(0),
                sourceLocale.getLocaleId());

        when(textFlowDAO.getByHash(hash)).thenReturn(null);
        when(textFlowDAO.persist(expectedTf)).thenReturn(expectedTf);
        when(textFlowTargetDAO.persist(expectedTft)).thenReturn(expectedTft);

        when(msProvider.translate(sources, sourceLocale, targetLocale,
                MediaType.TEXT_PLAIN_TYPE))
                        .thenReturn(expectedTranslations);

        List<String> translations =
                persistentTranslationService.translate(sources, sourceLocale,
                        targetLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE);

        verify(msProvider).translate(sources, sourceLocale, targetLocale,
                MediaType.TEXT_PLAIN_TYPE);
        verify(textFlowDAO).getByHash(hash);
        verify(textFlowTargetDAO).persist(expectedTft);
        assertThat(translations).isEqualTo(
                expectedTranslations.stream().map(AugmentedTranslation::getPlainTranslation).collect(
                        Collectors.toList()));
    }

    @Test
    public void testTranslationExists() throws BadRequestException {
        String source = "string to translate";
        String expectedTranslation = "translation of:" + source;
        String expectedRawContent =
                "<MSString>" + expectedTranslation + "</MSString>";
        Locale sourceLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = new Locale(LocaleId.DE, "German");

        TextFlow expectedTf = new TextFlow(source, sourceLocale);
        TextFlowTarget expectedTft = new TextFlowTarget(expectedTranslation,
                expectedRawContent, expectedTf, targetLocale, BackendID.MS);
        expectedTf.getTargets().add(expectedTft);

        String hash = HashUtil.generateHash(source,
                sourceLocale.getLocaleId());

        when(textFlowDAO.getByHash(hash)).thenReturn(expectedTf);

        String translation =
                persistentTranslationService
                    .translate(source, sourceLocale, targetLocale,
                        BackendID.MS, MediaType.TEXT_PLAIN_TYPE);

        verify(textFlowDAO).getByHash(hash);
        verify(textFlowTargetDAO).persist(expectedTft);
        verifyZeroInteractions(msProvider);
        assertThat(translation).isEqualTo(expectedTranslation);
    }
}
