package org.zanata.mt.service;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.dao.TextFlowDAO;
import org.zanata.mt.dao.TextFlowTargetDAO;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.service.impl.MicrosoftProvider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.zanata.mt.service.TranslationService.MAX_LENGTH;
import static org.zanata.mt.service.impl.MicrosoftProvider.AZURE_ID;
import static org.zanata.mt.service.impl.MicrosoftProvider.AZURE_SECRET;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiRunner.class)
public class TranslationServiceTest {

    @Mock
    private TextFlowDAO textFlowDAO;

    @Mock
    private TextFlowTargetDAO textFlowTargetDAO;

    @Mock
    private MicrosoftProvider msProvider;

    private TranslationService translationService;

    @BeforeClass
    public static void beforeClass() {
        String id = "id";
        String secret = "secret";
        System.setProperty(AZURE_ID, id);
        System.setProperty(AZURE_SECRET, secret);
    }

    @Before
    public void setup() {
        translationService =
            new TranslationService(textFlowDAO, textFlowTargetDAO,
                msProvider);
    }

    @Test
    public void testValidateLength() throws BadRequestException {
        String overLengthSource = StringUtils.repeat("t", MAX_LENGTH + 1);
        Locale sourceLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = new Locale(LocaleId.DE, "German");
        String translations = translationService.translate(overLengthSource,
                sourceLocale, targetLocale, BackendID.MS,
                MediaType.TEXT_PLAIN_TYPE);
        assertThat(translations).isEqualTo(overLengthSource);
    }

    @Test
    public void testValidateEmptySrcLocale() {
        String source = "testing source";
        Locale sourceLocale = null;
        Locale targetLocale = new Locale(LocaleId.DE, "German");

        assertThatThrownBy(() -> translationService.translate(source,
                sourceLocale, targetLocale,
                BackendID.MS, MediaType.TEXT_PLAIN_TYPE))
                        .isInstanceOf(BadRequestException.class);
    }

    @Test
    public void testValidateEmptyTargetLocale() {
        String source = "testing source";
        Locale sourceLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = null;

        assertThatThrownBy(() -> translationService.translate(source,
                sourceLocale, targetLocale,
                BackendID.MS, MediaType.TEXT_PLAIN_TYPE))
                        .isInstanceOf(BadRequestException.class);
    }

    @Test
    public void testValidateEmptyProvider() {
        String source = "testing source";
        Locale sourceLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = new Locale(LocaleId.DE, "German");

        assertThatThrownBy(() -> translationService.translate(source,
                sourceLocale, targetLocale, null, MediaType.TEXT_PLAIN_TYPE))
                        .isInstanceOf(BadRequestException.class);
    }
}
