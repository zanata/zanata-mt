package org.zanata.mt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.mt.api.service.DocumentContentTranslatorResource.MAX_LENGTH;

import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.model.Document;
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
        String overMaxHTML = "<pre>" + StringUtils.repeat("t",
                MAX_LENGTH) + "</pre>";

        List<String> htmls =
                Lists.newArrayList("<div>Entry 1</div>",
                        "<div>Entry 2</div>",
                        "<div>Entry 5</div>",
                        "<pre>KCS code section</pre>",
                        "<div translate=\"no\">non translatable node</div>",
                        "<div id=\"private-notes\"><span>private notes</span></div>",
                        overMaxHTML);

        List<String> processedHtmls =
                Lists.newArrayList("<div>\n Entry 1\n</div>",
                        "<div>\n Entry 2\n</div>",
                        "<div>\n Entry 5\n</div>",
                        "<span id=\"ZNTA-5-0\" translate=\"no\"></span>",
                        "<span id=\"ZNTA-6-0\" translate=\"no\"></span>",
                        "<span id=\"ZNTA-7-0\" translate=\"no\"></span>",
                        "<span id=\"ZNTA-8-0\" translate=\"no\"></span>");

        List<String> text = Lists.newArrayList("Entry 3", "Entry 4");

        List<String> translatedHtmls =
                Lists.newArrayList("<div>\n  MS: Entry 1 \n</div>",
                        "<div>\n  MS: Entry 2 \n</div>",
                        "<div>\n  MS: Entry 5 \n</div>",
                        "<span id=\"ZNTA-5-0\" translate=\"no\"></span>",
                        "<span id=\"ZNTA-6-0\" translate=\"no\"></span>",
                        "<span id=\"ZNTA-7-0\" translate=\"no\"></span>",
                        "<span id=\"ZNTA-8-0\" translate=\"no\"></span>");

        List<String> translatedText = Lists.newArrayList("MS: Entry 3", "MS: Entry 4");

        List<TypeString> contents = Lists.newArrayList(
                new TypeString(htmls.get(0), MediaType.TEXT_HTML, "meta1"),
                new TypeString(htmls.get(1), MediaType.TEXT_HTML, "meta2"),
                new TypeString(text.get(0), MediaType.TEXT_PLAIN, "meta3"),
                new TypeString(text.get(1), MediaType.TEXT_PLAIN, "meta4"),
                new TypeString(htmls.get(2), MediaType.TEXT_HTML, "meta5"),
                new TypeString(htmls.get(3), MediaType.TEXT_HTML, "meta6"),
                new TypeString(htmls.get(4), MediaType.TEXT_HTML, "meta7"),
                new TypeString(htmls.get(5), MediaType.TEXT_HTML, "meta8"),
                new TypeString(htmls.get(6), MediaType.TEXT_HTML, "meta9"));

        List<TypeString> translatedContents = Lists.newArrayList(
                new TypeString(translatedHtmls.get(0), MediaType.TEXT_HTML, "meta1"),
                new TypeString(translatedHtmls.get(1), MediaType.TEXT_HTML, "meta2"),
                new TypeString(translatedText.get(0), MediaType.TEXT_PLAIN, "meta3"),
                new TypeString(translatedText.get(1), MediaType.TEXT_PLAIN, "meta4"),
                new TypeString(translatedHtmls.get(2), MediaType.TEXT_HTML, "meta5"),
                new TypeString(translatedHtmls.get(3), MediaType.TEXT_HTML, "meta6"),
                new TypeString(translatedHtmls.get(4), MediaType.TEXT_HTML, "meta7"),
                new TypeString(translatedHtmls.get(5), MediaType.TEXT_HTML, "meta8"));

        DocumentContent
                docContent = new DocumentContent(contents, "http://localhost", "en");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        when(persistentTranslationService.translate(document, processedHtmls,
                srcLocale, transLocale, BackendID.MS, MediaType.TEXT_HTML_TYPE))
                .thenReturn(translatedHtmls);

        when(persistentTranslationService.translate(document, text,
                srcLocale, transLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE))
                .thenReturn(translatedText);

        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);

        assertThat(translatedDocContent.getLocale())
                .isEqualTo(transLocale.getLocaleId().getId());
        assertThat(translatedDocContent.getBackendId()).isEqualTo(BackendID.MS.getId());
        assertThat(translatedDocContent.getUrl()).isEqualTo(docContent.getUrl());

        assertThat(translatedDocContent.getContents().get(0))
                .isEqualTo(translatedContents.get(0));

        assertThat(translatedDocContent.getContents().get(1))
                .isEqualTo(translatedContents.get(1));

        assertThat(translatedDocContent.getContents().get(2))
                .isEqualTo(translatedContents.get(2));

        assertThat(translatedDocContent.getContents().get(3))
                .isEqualTo(translatedContents.get(3));

        assertThat(translatedDocContent.getContents().get(4))
                .isEqualTo(translatedContents.get(4));

        assertThat(translatedDocContent.getContents().get(5).getValue().trim()
                .replaceAll(">\\s+", ">").replaceAll("\\s+<", "<"))
                .isEqualTo(htmls.get(3));

        assertThat(translatedDocContent.getContents().get(6).getValue().trim()
                .replaceAll(">\\s+", ">").replaceAll("\\s+<", "<"))
                .isEqualTo(htmls.get(4));

        verify(persistentTranslationService)
                .translate(document, processedHtmls, srcLocale, transLocale, BackendID.MS,
                        MediaType.TEXT_HTML_TYPE);

        verify(persistentTranslationService)
                .translate(document, text, srcLocale, transLocale, BackendID.MS,
                        MediaType.TEXT_PLAIN_TYPE);
    }
}
