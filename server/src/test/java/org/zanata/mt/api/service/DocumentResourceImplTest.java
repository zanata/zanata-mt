package org.zanata.mt.api.service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.DocumentStatistics;
import org.zanata.mt.api.dto.LocaleCode;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.api.service.impl.DocumentResourceImpl;
import org.zanata.mt.dao.DocumentDAO;
import org.zanata.mt.dao.LocaleDAO;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.process.DocumentProcessKey;
import org.zanata.mt.process.DocumentProcessManager;
import org.zanata.mt.service.DocumentContentTranslatorService;
import org.zanata.mt.service.ConfigurationService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentResourceImplTest {

    private DocumentResource documentResource;

    @Mock
    private DocumentContentTranslatorService documentContentTranslatorService;

    @Mock
    private LocaleDAO localeDAO;

    @Mock
    private DocumentDAO documentDAO;

    @Mock
    private DocumentProcessManager docProcessLock;

    @Mock
    private ConfigurationService configurationService;

    @Before
    public void beforeTest() {
        documentResource =
                new DocumentResourceImpl(documentContentTranslatorService,
                        localeDAO, documentDAO, docProcessLock,
                        configurationService);
        when(documentContentTranslatorService
                .isMediaTypeSupported("text/plain")).thenReturn(true);
        when(documentContentTranslatorService.isMediaTypeSupported("text/html"))
                .thenReturn(true);
    }

    @Test
    public void testConstructor() {
        DocumentResource
                resource = new DocumentResourceImpl();
    }

    @Test
    public void testStatisticsNullUrl() {
        LocaleCode fromLocaleCode = LocaleCode.EN_US;
        LocaleCode toLocaleCode = LocaleCode.DE;
        Response response = documentResource
                .getStatistics(null, fromLocaleCode, toLocaleCode, null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testStatisticsAll() {
        String url = "http://localhost";

        LocaleCode fromLocaleCode = LocaleCode.EN_US;
        Locale fromLocale = new Locale(fromLocaleCode, "English");
        LocaleCode toLocaleCode = LocaleCode.DE;
        Locale toLocale = new Locale(toLocaleCode, "German");

        List<Document> expectedDocList = Lists.newArrayList();
        Document document1 = new Document(url, fromLocale, toLocale);
        document1.incrementCount();
        Document document2 = new Document(url, toLocale, fromLocale);
        document2.incrementCount();
        expectedDocList.add(document1);
        expectedDocList.add(document2);

        when(documentDAO.getByUrl(url, Optional.empty(), Optional.empty(),
                Optional.empty()))
                .thenReturn(expectedDocList);

        Response response = documentResource
                .getStatistics(url, null, null, null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        DocumentStatistics docStats = (DocumentStatistics)response.getEntity();
        assertThat(docStats.getUrl()).isEqualTo(url);
        assertThat(docStats.getRequestCounts().size())
                .isEqualTo(expectedDocList.size());
        verify(documentDAO).getByUrl(url, Optional.empty(), Optional.empty(),
                Optional.empty());
    }

    @Test
    public void testStatisticsWithFromLocale() {
        String url = "http://localhost";

        LocaleCode fromLocaleCode = LocaleCode.EN_US;
        Locale fromLocale = new Locale(fromLocaleCode, "English");
        LocaleCode toLocaleCode = LocaleCode.DE;
        Locale toLocale = new Locale(toLocaleCode, "German");

        List<Document> expectedDocList = Lists.newArrayList();
        Document document1 = new Document(url, fromLocale, toLocale);
        document1.incrementCount();
        expectedDocList.add(document1);

        when(documentDAO
                .getByUrl(url, Optional.of(fromLocaleCode), Optional.empty(),
                        Optional.empty()))
                .thenReturn(expectedDocList);

        Response response = documentResource
                .getStatistics(url, fromLocaleCode, null, null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        DocumentStatistics docStats = (DocumentStatistics)response.getEntity();
        assertThat(docStats.getUrl()).isEqualTo(url);
        assertThat(docStats.getRequestCounts().size())
                .isEqualTo(expectedDocList.size());
        verify(documentDAO)
                .getByUrl(url, Optional.of(fromLocaleCode), Optional.empty(), Optional.empty());
    }

    @Test
    public void testStatisticsWithToLocale () {
        String url = "http://localhost";

        LocaleCode fromLocaleCode = LocaleCode.EN_US;
        Locale fromLocale = new Locale(fromLocaleCode, "English");
        LocaleCode toLocaleCode = LocaleCode.DE;
        Locale toLocale = new Locale(toLocaleCode, "German");

        List<Document> expectedDocList = Lists.newArrayList();
        Document document1 = new Document(url, fromLocale, toLocale);
        document1.incrementCount();
        expectedDocList.add(document1);

        when(documentDAO
                .getByUrl(url, Optional.empty(), Optional.of(toLocaleCode),
                        Optional.empty()))
                .thenReturn(expectedDocList);

        Response response = documentResource
                .getStatistics(url, null, toLocaleCode, null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        DocumentStatistics docStats = (DocumentStatistics)response.getEntity();
        assertThat(docStats.getUrl()).isEqualTo(url);
        assertThat(docStats.getRequestCounts().size())
                .isEqualTo(expectedDocList.size());
        verify(documentDAO)
                .getByUrl(url, Optional.empty(), Optional.of(toLocaleCode),
                        Optional.empty());
    }

    @Test
    public void testTranslateDocumentContentBadParams() {
        DocumentContent docContent = new DocumentContent(null, null, null);
        // empty trans locale
        Response response = documentResource
                .translate(docContent, null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testInvalidTranslateDocRequest() {
        // null docContent
        Response response =
                documentResource.translate(null, null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty fields
        DocumentContent documentContent = new DocumentContent(null, null, null);
        response = documentResource.translate(documentContent,
                LocaleCode.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // docContent with no content
        documentContent = new DocumentContent(null, null, null);
        response = documentResource
                .translate(documentContent, LocaleCode.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty locale
        documentContent = new DocumentContent(
                Lists.newArrayList(new TypeString("string", "text/plain", "meta")),
                "http://localhost", null);
        response = documentResource
                .translate(documentContent, LocaleCode.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // docContent with content but no url
        documentContent = new DocumentContent(Lists.newArrayList(new TypeString("test",
                MediaType.TEXT_PLAIN, "meta")), null, "en");
        response = documentResource
                .translate(documentContent, LocaleCode.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty content in typeString
        List<TypeString> strings = Lists.newArrayList(
                new TypeString("", "text/plain", "meta"));
        documentContent =
                new DocumentContent(strings, "http://localhost", "en");
        response = documentResource
                .translate(documentContent, LocaleCode.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty type in typeString
        strings = Lists.newArrayList(
                new TypeString("test", "", "meta"));
        documentContent =
                new DocumentContent(strings, "http://localhost", "en");
        response = documentResource
                .translate(documentContent, LocaleCode.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // invalid type in typeString
        strings = Lists.newArrayList(
                new TypeString("test", "text/invalid", "meta"));
        documentContent =
                new DocumentContent(strings, "http://localhost", "en");
        response = documentResource
                .translate(documentContent, LocaleCode.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testSameLocale() {
        Locale locale = new Locale(LocaleCode.EN, "English");

        DocumentContent
                docContent = new DocumentContent(Lists.newArrayList(
                new TypeString("testing", MediaType.TEXT_HTML, "meta1")),
                "http://localhost", locale.getLocaleCode().getId());

        Response response =
                documentResource
                        .translate(docContent, locale.getLocaleCode());

        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());

        DocumentContent returnedDocContent =
                (DocumentContent) response.getEntity();

        assertThat(returnedDocContent).isEqualTo(docContent);
    }

    @Test
    public void testTranslateDocumentContent() {
        Locale fromLocale = new Locale(LocaleCode.EN, "English");
        Locale toLocale = new Locale(LocaleCode.DE, "German");

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
        DocumentContent translatedDocContent =
                new DocumentContent(translatedContents, "http://localhost",
                        toLocale.getLocaleCode().getId());

        org.zanata.mt.model.Document
                doc = Mockito.mock(org.zanata.mt.model.Document.class);

        when(localeDAO.getByLocaleCode(fromLocale.getLocaleCode()))
                .thenReturn(fromLocale);
        when(localeDAO.getByLocaleCode(toLocale.getLocaleCode()))
                .thenReturn(toLocale);
        when(documentDAO.getOrCreateByUrl(docContent.getUrl(), fromLocale,
                toLocale)).thenReturn(doc);

        when(documentContentTranslatorService
                .translateDocument(doc, docContent, BackendID.MS,
                        DocumentResource.MAX_LENGTH))
                .thenReturn(translatedDocContent);

        Response response =
                documentResource
                        .translate(docContent, toLocale.getLocaleCode());

        assertThat(response.getStatus())
                    .isEqualTo(Response.Status.OK.getStatusCode());
        DocumentContent returnedDocContent = (DocumentContent)response.getEntity();

        assertThat(returnedDocContent.getContents()).isEqualTo(translatedContents);
        assertThat(returnedDocContent.getLocaleCode())
                .isEqualTo(toLocale.getLocaleCode().getId());

        DocumentProcessKey key =
                new DocumentProcessKey(docContent.getUrl(),
                        fromLocale.getLocaleCode(), toLocale.getLocaleCode());
        verify(docProcessLock).lock(key);
        verify(doc).incrementCount();
        verify(documentDAO).persist(doc);
    }

    @Test
    public void testDevMode() {
        Locale fromLocale = new Locale(LocaleCode.EN, "English");
        Locale toLocale = new Locale(LocaleCode.DE, "German");

        List<TypeString> contents = Lists.newArrayList(
                new TypeString("<html><body>Entry 1</body></html>",
                        MediaType.TEXT_HTML, "meta1"));

        org.zanata.mt.model.Document
                doc = Mockito.mock(org.zanata.mt.model.Document.class);

        DocumentContent
                docContent = new DocumentContent(contents, "http://localhost",
                fromLocale.getLocaleCode().getId());

        when(configurationService.isDevMode()).thenReturn(true);
        when(localeDAO.getByLocaleCode(fromLocale.getLocaleCode()))
                .thenReturn(fromLocale);
        when(localeDAO.getByLocaleCode(toLocale.getLocaleCode()))
                .thenReturn(toLocale);
        when(documentDAO.getOrCreateByUrl(docContent.getUrl(), fromLocale,
                toLocale)).thenReturn(doc);

        documentResource
                .translate(docContent, toLocale.getLocaleCode());
        verify(documentContentTranslatorService)
                .translateDocument(doc, docContent, BackendID.DEV,
                        DocumentResource.MAX_LENGTH);
    }
}
