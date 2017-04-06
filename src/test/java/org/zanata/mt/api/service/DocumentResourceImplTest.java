package org.zanata.mt.api.service;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.DocumentStatistics;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.api.service.impl.DocumentResourceImpl;
import org.zanata.mt.dao.DocumentDAO;
import org.zanata.mt.dao.LocaleDAO;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.service.DocumentContentTranslatorService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.mt.api.service.impl.DocumentResourceImpl.MAX_LENGTH;

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

    @Before
    public void beforeTest() {
        documentResource =
                new DocumentResourceImpl(documentContentTranslatorService, localeDAO, documentDAO);
        when(documentContentTranslatorService.isMediaTypeSupported("text/plain")).thenReturn(true);
        when(documentContentTranslatorService.isMediaTypeSupported("text/html")).thenReturn(true);
    }

    @Test
    public void testConstructor() {
        DocumentResource
                resource = new DocumentResourceImpl();
    }

    @Test
    public void testStatisticsNullUrl() {
        LocaleId fromLocaleCode = LocaleId.EN_US;
        LocaleId toLocaleCode = LocaleId.DE;
        Response response = documentResource
                .getStatistics(null, fromLocaleCode, toLocaleCode);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testStatisticsAll() {
        String url = "http://localhost";

        LocaleId fromLocaleCode = LocaleId.EN_US;
        Locale fromLocale = new Locale(fromLocaleCode, "English");
        LocaleId toLocaleCode = LocaleId.DE;
        Locale toLocale = new Locale(toLocaleCode, "German");

        List<Document> expectedDocList = Lists.newArrayList();
        Document document1 = new Document(url, fromLocale, toLocale);
        document1.incrementUsedCount();
        Document document2 = new Document(url, toLocale, fromLocale);
        document2.incrementUsedCount();
        expectedDocList.add(document1);
        expectedDocList.add(document2);

        when(documentDAO.getByUrl(url, Optional.empty(), Optional.empty()))
                .thenReturn(expectedDocList);

        Response response = documentResource
                .getStatistics(url, null, null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        DocumentStatistics docStats = (DocumentStatistics)response.getEntity();
        assertThat(docStats.getUrl()).isEqualTo(url);
        assertThat(docStats.getRequestCounts().size())
                .isEqualTo(expectedDocList.size());
        verify(documentDAO).getByUrl(url, Optional.empty(), Optional.empty());
    }

    @Test
    public void testStatisticsWithFromLocale() {
        String url = "http://localhost";

        LocaleId fromLocaleCode = LocaleId.EN_US;
        Locale fromLocale = new Locale(fromLocaleCode, "English");
        LocaleId toLocaleCode = LocaleId.DE;
        Locale toLocale = new Locale(toLocaleCode, "German");

        List<Document> expectedDocList = Lists.newArrayList();
        Document document1 = new Document(url, fromLocale, toLocale);
        document1.incrementUsedCount();
        expectedDocList.add(document1);

        when(documentDAO
                .getByUrl(url, Optional.of(fromLocaleCode), Optional.empty()))
                .thenReturn(expectedDocList);

        Response response = documentResource
                .getStatistics(url, fromLocaleCode, null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        DocumentStatistics docStats = (DocumentStatistics)response.getEntity();
        assertThat(docStats.getUrl()).isEqualTo(url);
        assertThat(docStats.getRequestCounts().size())
                .isEqualTo(expectedDocList.size());
        verify(documentDAO)
                .getByUrl(url, Optional.of(fromLocaleCode), Optional.empty());
    }

    @Test
    public void testStatisticsWithToLocale() {
        String url = "http://localhost";

        LocaleId fromLocaleCode = LocaleId.EN_US;
        Locale fromLocale = new Locale(fromLocaleCode, "English");
        LocaleId toLocaleCode = LocaleId.DE;
        Locale toLocale = new Locale(toLocaleCode, "German");

        List<Document> expectedDocList = Lists.newArrayList();
        Document document1 = new Document(url, fromLocale, toLocale);
        document1.incrementUsedCount();
        expectedDocList.add(document1);

        when(documentDAO
                .getByUrl(url, Optional.empty(), Optional.of(toLocaleCode)))
                .thenReturn(expectedDocList);

        Response response = documentResource
                .getStatistics(url, null, toLocaleCode);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        DocumentStatistics docStats = (DocumentStatistics)response.getEntity();
        assertThat(docStats.getUrl()).isEqualTo(url);
        assertThat(docStats.getRequestCounts().size())
                .isEqualTo(expectedDocList.size());
        verify(documentDAO)
                .getByUrl(url, Optional.empty(), Optional.of(toLocaleCode));
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
                LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // docContent with no content
        documentContent = new DocumentContent(null, null, null);
        response = documentResource
                .translate(documentContent, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty locale
        documentContent = new DocumentContent(
                Lists.newArrayList(new TypeString("string", "text/plain", "meta")),
                "http://localhost", null);
        response = documentResource
                .translate(documentContent, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // docContent with content but no url
        documentContent = new DocumentContent(Lists.newArrayList(new TypeString("test",
                MediaType.TEXT_PLAIN, "meta")), null, "en");
        response = documentResource
                .translate(documentContent, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty content in typeString
        List<TypeString> strings = Lists.newArrayList(
                new TypeString("", "text/plain", "meta"));
        documentContent =
                new DocumentContent(strings, "http://localhost", "en");
        response = documentResource
                .translate(documentContent, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty type in typeString
        strings = Lists.newArrayList(
                new TypeString("test", "", "meta"));
        documentContent =
                new DocumentContent(strings, "http://localhost", "en");
        response = documentResource
                .translate(documentContent, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // invalid type in typeString
        strings = Lists.newArrayList(
                new TypeString("test", "text/invalid", "meta"));
        documentContent =
                new DocumentContent(strings, "http://localhost", "en");
        response = documentResource
                .translate(documentContent, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // max length
        String overLengthSource = StringUtils.repeat("t", MAX_LENGTH + 1);
        strings = Lists.newArrayList(
                new TypeString(overLengthSource, "text/plain", "meta"));
        documentContent =
                new DocumentContent(strings, "http://localhost", "en");
        response = documentResource
                .translate(documentContent, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testTranslateSameLocale() {
        Locale locale = new Locale(LocaleId.EN, "English");

        DocumentContent
                docContent = new DocumentContent(Lists.newArrayList(
                new TypeString("testing", MediaType.TEXT_HTML, "meta1")),
                "http://localhost", locale.getLocaleId().getId());

        Response response =
                documentResource
                        .translate(docContent, locale.getLocaleId());

        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());

        DocumentContent returnedDocContent =
                (DocumentContent) response.getEntity();

        assertThat(returnedDocContent).isEqualTo(docContent);
    }

    @Test
    public void testTranslateDocumentContentBackendFailed() {
        testDocumentContentFailed(new BadRequestException(), Response.Status.BAD_REQUEST);
    }

    @Test
    public void testTranslateDocumentContentInternalError() {
        testDocumentContentFailed(new ZanataMTException("error"), Response.Status.INTERNAL_SERVER_ERROR);
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
        DocumentContent translatedDocContent =
                new DocumentContent(translatedContents, "http://localhost",
                        transLocale.getLocaleId().getId());

        org.zanata.mt.model.Document
                doc = Mockito.mock(org.zanata.mt.model.Document.class);

        when(localeDAO.getByLocaleId(srcLocale.getLocaleId()))
                .thenReturn(srcLocale);
        when(localeDAO.getByLocaleId(transLocale.getLocaleId()))
                .thenReturn(transLocale);
        when(documentDAO.getOrCreateByUrl(docContent.getUrl(), srcLocale,
                transLocale)).thenReturn(doc);

        when(documentContentTranslatorService
                .translateDocument(new Document(), docContent, srcLocale,
                transLocale, BackendID.MS)).thenReturn(translatedDocContent);

        Response response =
                documentResource
                        .translate(docContent, transLocale.getLocaleId());

        assertThat(response.getStatus())
                    .isEqualTo(Response.Status.OK.getStatusCode());
        DocumentContent returnedDocContent = (DocumentContent)response.getEntity();

        assertThat(returnedDocContent.getContents()).isEqualTo(translatedContents);
        assertThat(returnedDocContent.getLocaleCode())
                .isEqualTo(transLocale.getLocaleId().getId());
        verify(doc).incrementUsedCount();
        verify(documentDAO).persist(doc);
    }

    private void testDocumentContentFailed(Exception expectedException,
            Response.Status expectedStatus) {
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale transLocale = new Locale(LocaleId.DE, "German");

        List<TypeString> contents = Lists.newArrayList(
                new TypeString("<html>test</html>", MediaType.TEXT_HTML,
                        "meta"));
        DocumentContent
                documentContent = new DocumentContent(contents, "http://localhost",
                srcLocale.getLocaleId().getId());
        Document doc = new Document();

        when(documentDAO.getOrCreateByUrl(documentContent.getUrl(), srcLocale,
                transLocale)).thenReturn(doc);
        when(localeDAO.getByLocaleId(srcLocale.getLocaleId()))
                .thenReturn(srcLocale);
        when(localeDAO.getByLocaleId(transLocale.getLocaleId()))
                .thenReturn(transLocale);

        doThrow(expectedException).when(documentContentTranslatorService)
                .translateDocument(doc, documentContent, srcLocale,
                        transLocale, BackendID.MS);

        Response response =
                documentResource
                        .translate(documentContent, transLocale.getLocaleId());

        assertThat(response.getStatus())
                .isEqualTo(expectedStatus.getStatusCode());
    }

}
