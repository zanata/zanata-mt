package org.zanata.magpie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.api.dto.DocumentContent;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.api.dto.TypeString;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;

import com.google.common.collect.ImmutableList;
import org.zanata.magpie.model.StringType;
import org.zanata.magpie.util.SegmentString;
import org.zanata.magpie.util.ShortString;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocumentContentTranslatorServiceTest {

    private DocumentContentTranslatorService documentContentTranslatorService;

    @Mock
    private PersistentTranslationService persistentTranslationService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
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
    public void testLongHTML() {
        int maxLength = 25;
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        String html = "<div><span>content1</span><span>content2</span></div>";
        String expectedHtml = "<div><span>translated</span><span>translated</span></div>";

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(ImmutableList.of("<span>translated</span>"));

        List<TypeString> contents = ImmutableList.of(
                new TypeString(html, MediaType.TEXT_HTML, "meta"));
        DocumentContent docContent =
                new DocumentContent(contents, "http://localhost", "en");


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);
        assertThat(translatedDocContent.getContents().get(0).getValue()).isEqualTo(expectedHtml);
    }

    @Test
    public void testLongHTML2() {
        int maxLength = 25;
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        String html = "<span>content1</span><span>content2</span>";
        String expectedHtml = "<span>translated</span><span>translated</span>";

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);


        when(persistentTranslationService.translate(any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(ImmutableList.of("<span>translated</span>"));

        List<TypeString> contents = ImmutableList.of(
                new TypeString(html, MediaType.TEXT_HTML, "meta"));
        DocumentContent docContent =
                new DocumentContent(contents, "http://localhost", "en");


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);
        assertThat(translatedDocContent.getContents().get(0).getValue()).isEqualTo(expectedHtml);
    }

    @Test
    public void testLongHTMLCannotTranslate() {
        int maxLength = 25;
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        String html = "<div><span>content1</span><span>content too long cannot be translated</span></div>";
        String expectedHtml = "<div><span>translated</span><span>content too long cannot be translated</span></div>";

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(ImmutableList.of("<span>translated</span>"));

        List<TypeString> contents = ImmutableList.of(
                new TypeString(html, MediaType.TEXT_HTML, "meta"));
        DocumentContent docContent =
                new DocumentContent(contents, "http://localhost", "en");


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);
        assertThat(translatedDocContent.getContents().get(0).getValue())
                .isEqualTo(expectedHtml);
        assertThat(translatedDocContent.getWarnings()).hasSize(2);
    }

    @Test
    public void testXMLTags() {
        int maxLength = 250;
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        String html = "The <literal>@watch</literal> annotation is not working with accumulate in rules. [<link xlink:href=\"https://issues.jboss.org/browse/RHDM-509\">RHDM-509</link>]";
        String expectedHtml = "translated[网 The <literal>@watch</literal> annotation is not working with accumulate in rules. [<link xlink:href=\"https://issues.jboss.org/browse/RHDM-509\">RHDM-509</link>] 网]";

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(ImmutableList.of(expectedHtml));

        List<TypeString> contents = ImmutableList.of(
                new TypeString(html, MediaType.TEXT_XML, "meta"));
        DocumentContent docContent =
                new DocumentContent(contents, "http://localhost", "en");


        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);
        assertThat(translatedDocContent.getContents().get(0).getValue()).isEqualTo(expectedHtml);
    }

    @Test
    public void testTranslateLongPlainText() {
        int maxLength = 50;

        // strings with 6 sentences
        String text = "Hurry! I am never at home on Sundays. The mysterious diary records the voice. She was too short to see over the fence. Everyone was busy, so I went to the movie alone. Sometimes it is better to just walk away from things and go back to them later when you’re in a better frame of mind.";
        Locale fromLocale = new Locale(LocaleCode.EN, "English");
        Locale toLocale = new Locale(LocaleCode.DE, "German");
        Document document =
                new Document("http://localhost", fromLocale, toLocale);
        List<String> strings = SegmentString.segmentString(text, Optional.empty());

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(document,
                strings.subList(0, 2), fromLocale, toLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Translated:Hurray!", "Translated:I am never at home on Sundays. "));

        when(persistentTranslationService.translate(document,
                strings.subList(2, 3), fromLocale, toLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Translated:The mysterious diary records the voice. "));

        when(persistentTranslationService.translate(document,
                strings.subList(3, 4), fromLocale, toLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Translated:She was too short to see over the fence. "));

        when(persistentTranslationService.translate(document,
                strings.subList(4, 5), fromLocale, toLocale, BackendID.MS,
                StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(ImmutableList.of("Translated:Everyone was busy, so I went to the movie alone. "));

        DocumentContent docContent =
                new DocumentContent(ImmutableList.of(
                        new TypeString(text, MediaType.TEXT_PLAIN, "meta")), "http://localhost", "en");

        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);
        assertThat(translatedDocContent.getContents()).hasSize(1);
        assertThat(StringUtils.countMatches(translatedDocContent.getContents().get(0).getValue(), "Translated")).isEqualTo(5);
    }

    @Test
    public void testTranslateDocumentContent() {
        int maxLength = 10000;

        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale transLocale = new Locale(LocaleCode.DE, "German");

        String longText = StringUtils.repeat("5", maxLength);
        String maxString = StringUtils.repeat("t", maxLength + 1);

        List<String> htmls =
                ImmutableList.of("<div>Entry 1</div>",
                        "<div>Entry 2</div>",
                        "<div>Entry 6</div>",
                        "<code>KCS code section</code>",
                        "<div translate=\"no\">non translatable node</div>",
                        "<div id=\"private-notes\"><span>private notes</span></div>");

        List<String> processedHtmls =
                ImmutableList.of("<div>Entry 1</div>",
                        "<div>Entry 2</div>",
                        "<div>Entry 6</div>",
                        "<var id=\"ZNTA-6-0\" translate=\"no\"></var>",
                        "<var id=\"ZNTA-7-0\" translate=\"no\"></var>",
                        "<var id=\"ZNTA-8-0\" translate=\"no\"></var>");

        List<String> text = ImmutableList.of("Entry 3", "Entry 4", longText);

        List<String> translatedHtmls =
                ImmutableList.of("<div>MS: Entry 1</div>",
                        "<div>MS: Entry 2</div>",
                        "<div>MS: Entry 6</div>",
                        "<var id=\"ZNTA-6-0\" translate=\"no\"></var>",
                        "<var id=\"ZNTA-7-0\" translate=\"no\"></var>",
                        "<var id=\"ZNTA-8-0\" translate=\"no\"></var>");

        List<String> translatedText = ImmutableList.of("MS: Entry 3", "MS: Entry 4", "MS: Long text");

        List<TypeString> contents = ImmutableList.of(
                new TypeString(htmls.get(0), MediaType.TEXT_HTML, "meta1"),
                new TypeString(htmls.get(1), MediaType.TEXT_HTML, "meta2"),
                new TypeString(text.get(0), MediaType.TEXT_PLAIN, "meta3"),
                new TypeString(text.get(1), MediaType.TEXT_PLAIN, "meta4"),
                new TypeString(text.get(2), MediaType.TEXT_PLAIN, "meta5"),
                new TypeString(htmls.get(2), MediaType.TEXT_HTML, "meta6"),
                new TypeString(htmls.get(3), MediaType.TEXT_HTML, "meta7"),
                new TypeString(htmls.get(4), MediaType.TEXT_HTML, "meta8"),
                new TypeString(htmls.get(5), MediaType.TEXT_HTML, "meta9"),
                new TypeString(maxString, MediaType.TEXT_PLAIN, "meta10"));

        List<TypeString> translatedContents = ImmutableList.of(
                new TypeString(translatedHtmls.get(0), MediaType.TEXT_HTML, "meta1"),
                new TypeString(translatedHtmls.get(1), MediaType.TEXT_HTML, "meta2"),
                new TypeString(translatedText.get(0), MediaType.TEXT_PLAIN, "meta3"),
                new TypeString(translatedText.get(1), MediaType.TEXT_PLAIN, "meta4"),
                new TypeString(translatedText.get(2), MediaType.TEXT_PLAIN, "meta5"),
                new TypeString(translatedHtmls.get(2), MediaType.TEXT_HTML, "meta6"),
                new TypeString(htmls.get(3), MediaType.TEXT_HTML, "meta7"),
                new TypeString(htmls.get(4), MediaType.TEXT_HTML, "meta8"),
                new TypeString(htmls.get(5), MediaType.TEXT_HTML, "meta9"));

        DocumentContent
                docContent = new DocumentContent(contents, "http://localhost", "en");
        Document document =
                new Document("http://localhost", srcLocale, transLocale);

        when(persistentTranslationService.getMaxLength(BackendID.MS))
                .thenReturn(maxLength);

        when(persistentTranslationService.translate(document, processedHtmls,
                srcLocale, transLocale, BackendID.MS,
                StringType.HTML, Optional.of("tech"))).thenReturn(translatedHtmls);

        when(persistentTranslationService
                .translate(document, text.subList(0, 2),
                        srcLocale, transLocale, BackendID.MS,
                        StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(translatedText.subList(0, 2));

        when(persistentTranslationService
                .translate(document, text.subList(2, 3),
                        srcLocale, transLocale, BackendID.MS,
                        StringType.TEXT_PLAIN, Optional.of("tech")))
                .thenReturn(translatedText.subList(2, 3));

        DocumentContent translatedDocContent = documentContentTranslatorService
                .translateDocument(document, docContent, BackendID.MS);

        assertThat(translatedDocContent.getLocaleCode())
                .isEqualTo(transLocale.getLocaleCode().getId());
        assertThat(translatedDocContent.getBackendId()).isEqualTo(BackendID.MS.getId());
        assertThat(translatedDocContent.getUrl()).isEqualTo(docContent.getUrl());

        assertThat(translatedDocContent.getWarnings()).hasSize(1);
        assertThat(translatedDocContent.getWarnings().get(0).getDetails())
                .contains(ShortString.shorten(maxString));

        for (int i = 0; i < translatedDocContent.getContents().size() - 1; i++) {
            assertThat(translatedDocContent.getContents().get(i))
                    .isEqualTo(translatedContents.get(i));
        }

        int requestsCount = text.size();
        verify(persistentTranslationService,
                times(requestsCount))
                .translate(any(), anyList(), any(Locale.class),
                        any(Locale.class),
                        any(BackendID.class), any(StringType.class),
                        any());
    }
}
