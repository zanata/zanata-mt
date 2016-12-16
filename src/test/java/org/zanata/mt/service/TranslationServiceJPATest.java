package org.zanata.mt.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.dao.TextFlowDAO;
import org.zanata.mt.dao.TextFlowTargetDAO;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.Provider;
import org.zanata.mt.model.TextFlow;
import org.zanata.mt.model.TextFlowTarget;
import org.zanata.mt.model.ValueUnit;
import org.zanata.mt.service.impl.MicrosoftProvider;
import org.zanata.mt.util.TranslationUtil;

import com.google.common.collect.Lists;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class TranslationServiceJPATest {

    @Mock
    private TextFlowDAO textFlowDAO;

    @Mock
    private TextFlowTargetDAO textFlowTargetDAO;

    @Mock
    private MicrosoftProvider msProvider;

    private TranslationService translationService;

    @Before
    public void setup() {
        translationService =
                new TranslationService(textFlowDAO, textFlowTargetDAO,
                    msProvider);
    }

    @Test
    public void testNewTranslation()
            throws BadRequestException {
        List<String> sources = Lists.newArrayList("string to translate");
        List<ValueUnit> expectedTranslations =
                Lists.newArrayList(new ValueUnit(
                        "translation of:" + sources.get(0), "<MSString>"
                                + "translation of:" + sources.get(0) + "</MSString>"));
       
        Locale sourceLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = new Locale(LocaleId.DE, "German");
        TextFlow expectedTf = new TextFlow(sources.get(0), sourceLocale);
        TextFlowTarget expectedTft =
                new TextFlowTarget(expectedTranslations.get(0).getValue(),
                        expectedTranslations.get(0).getRawValue(), expectedTf,
                        targetLocale, Provider.MS);
        expectedTf.getTargets().add(expectedTft);
        String hash = TranslationUtil.generateHash(sources.get(0),
                sourceLocale.getLocaleId());

        when(textFlowDAO.getByHash(hash)).thenReturn(null);
        when(textFlowDAO.persist(expectedTf)).thenReturn(expectedTf);
        when(textFlowTargetDAO.persist(expectedTft)).thenReturn(expectedTft);

        when(msProvider.translate(sources, sourceLocale, targetLocale))
                .thenReturn(expectedTranslations);

        List<String> translations =
                translationService.translate(sources, sourceLocale,
                        targetLocale, Provider.MS);

        verify(msProvider).translate(sources, sourceLocale, targetLocale);
        verify(textFlowDAO).getByHash(hash);
        verify(textFlowDAO).persist(expectedTf);
        verify(textFlowTargetDAO).persist(expectedTft);
        assertThat(translations).isEqualTo(
                expectedTranslations.stream().map(ValueUnit::getValue).collect(
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
                expectedRawContent, expectedTf, targetLocale, Provider.MS);
        expectedTf.getTargets().add(expectedTft);

        String hash = TranslationUtil.generateHash(source,
                sourceLocale.getLocaleId());

        when(textFlowDAO.getByHash(hash)).thenReturn(expectedTf);

        String translation =
                translationService.translate(source, sourceLocale, targetLocale,
                        Provider.MS);

        verify(textFlowDAO).getByHash(hash);
        verify(textFlowTargetDAO).persist(expectedTft);
        verifyZeroInteractions(msProvider);
        assertThat(translation).isEqualTo(expectedTranslation);
    }
}
