package org.zanata.mt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.model.Locale;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentContentTranslatorServiceTest {

    private DocumentContentTranslatorService documentContentTranslatorService;

    @Mock
    private PersistentTranslationService persistentTranslationService;

    @Before
    public void setup() {
        documentContentTranslatorService =
                new DocumentContentTranslatorService(persistentTranslationService);
    }

    @Test
    public void testEmptyConstructor() {
        DocumentContentTranslatorService
                documentContentTranslatorService = new DocumentContentTranslatorService();
    }

    @Test
    public void testGetMediaType() {
        assertThatThrownBy(
                () -> documentContentTranslatorService.getMediaType("notSupportType"))
                .isInstanceOf(BadRequestException.class);

        String mediaType = "text/plain";
        assertThat(documentContentTranslatorService.getMediaType(mediaType)).isNotNull()
                .isEqualTo(MediaType.TEXT_PLAIN_TYPE);

        mediaType = "text/html";
        assertThat(documentContentTranslatorService.getMediaType(mediaType)).isNotNull()
                .isEqualTo(MediaType.TEXT_HTML_TYPE);
    }

    @Test
    public void testTranslateDocumentContent() {
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale transLocale = new Locale(LocaleId.DE, "German");

        List<String> htmls =
                Lists.newArrayList("<html><body>Entry 1</body></html>",
                        "<html><body>Entry 2</body></html>",
                        "<html><body>Entry 5</body></html>");
        List<String> text = Lists.newArrayList("Entry 3", "Entry 4");

        List<String> translatedHtmls =
                Lists.newArrayList("<html><body>MS: Entry 1</body></html>",
                        "<html><body>MS: Entry 2</body></html>",
                        "<html><body>MS: Entry 5</body></html>");
        List<String> translatedText = Lists.newArrayList("MS: Entry 3", "MS: Entry 4");

        List<TypeString> contents = Lists.newArrayList(
                new TypeString(htmls.get(0), MediaType.TEXT_HTML, "meta1"),
                new TypeString(htmls.get(1), MediaType.TEXT_HTML, "meta2"),
                new TypeString(text.get(0), MediaType.TEXT_PLAIN, "meta3"),
                new TypeString(text.get(1), MediaType.TEXT_PLAIN, "meta4"),
                new TypeString(htmls.get(2), MediaType.TEXT_HTML, "meta5"));

        List<TypeString> translatedContents = Lists.newArrayList(
                new TypeString(translatedHtmls.get(0), MediaType.TEXT_HTML, "meta1"),
                new TypeString(translatedHtmls.get(1), MediaType.TEXT_HTML, "meta2"),
                new TypeString(translatedText.get(0), MediaType.TEXT_PLAIN, "meta3"),
                new TypeString(translatedText.get(1), MediaType.TEXT_PLAIN, "meta4"),
                new TypeString(translatedHtmls.get(2), MediaType.TEXT_HTML, "meta5"));

        DocumentContent
                docContent = new DocumentContent(contents, "http://localhost", "en");

        when(persistentTranslationService.translate(htmls,
                srcLocale,
                transLocale, BackendID.MS, MediaType.TEXT_HTML_TYPE))
                .thenReturn(translatedHtmls);

        when(persistentTranslationService.translate(text,
                srcLocale,
                transLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE))
                .thenReturn(translatedText);


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(docContent, srcLocale, transLocale,
                        BackendID.MS);

        assertThat(translatedDocContent.getLocale())
                .isEqualTo(transLocale.getLocaleId().getId());
        assertThat(translatedDocContent.getBackendId()).isEqualTo(BackendID.MS.getId());
        assertThat(translatedDocContent.getUrl()).isEqualTo(docContent.getUrl());
        assertThat(translatedDocContent.getContents())
                .isEqualTo(translatedContents);

        verify(persistentTranslationService)
                .translate(htmls, srcLocale, transLocale, BackendID.MS,
                        MediaType.TEXT_HTML_TYPE);

        verify(persistentTranslationService)
                .translate(text, srcLocale, transLocale, BackendID.MS,
                        MediaType.TEXT_PLAIN_TYPE);
    }
}
