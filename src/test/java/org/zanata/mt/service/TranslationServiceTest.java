package org.zanata.mt.service;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jglue.cdiunit.CdiRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.exception.BadTranslationRequestException;
import org.zanata.mt.exception.TranslationProviderException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.Provider;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.zanata.mt.service.impl.MicrosoftTranslatorAPI.AZURE_ID;
import static org.zanata.mt.service.impl.MicrosoftTranslatorAPI.AZURE_SECRET;
import static org.zanata.mt.service.TranslationService.MAX_LENGTH;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiRunner.class)
public class TranslationServiceTest {

    @Inject
    private TranslationService translationService;

    @BeforeClass
    public static void beforeClass() {
        String id = "id";
        String secret = "secret";
        System.setProperty(AZURE_ID, id);
        System.setProperty(AZURE_SECRET, secret);
    }

    @Test
    public void testValidateLength()
        throws TranslationProviderException, BadTranslationRequestException {
        String overLengthSource = StringUtils.repeat("t", MAX_LENGTH + 1);
        Locale sourceLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = new Locale(LocaleId.DE, "German");
        String translations = translationService.translate(overLengthSource,
                sourceLocale, targetLocale, Provider.MS);
        assertThat(translations).isEqualTo(overLengthSource);
    }

    @Test(expected = BadTranslationRequestException.class)
    public void testValidateEmptySrcLocale()
        throws TranslationProviderException, BadTranslationRequestException {
        String source = "testing source";
        Locale sourceLocale = null;
        Locale targetLocale = new Locale(LocaleId.DE, "German");
        translationService.translate(source, sourceLocale, targetLocale,
                Provider.MS);
    }

    @Test(expected = BadTranslationRequestException.class)
    public void testValidateEmptyTargetLocale()
        throws TranslationProviderException, BadTranslationRequestException {
        String source = "testing source";
        Locale sourceLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = null;
        translationService.translate(source, sourceLocale, targetLocale,
                Provider.MS);
    }

    @Test(expected = BadTranslationRequestException.class)
    public void testValidateEmptyProvider()
        throws TranslationProviderException, BadTranslationRequestException {
        String source = "testing source";
        Locale sourceLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = new Locale(LocaleId.DE, "German");
        translationService.translate(source, sourceLocale, targetLocale, null);
    }
}
