package org.magpie.mt.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.magpie.mt.api.dto.LocaleCode;
import org.magpie.mt.dao.DocumentDAO;
import org.magpie.mt.model.Document;
import org.magpie.mt.model.Locale;

public class DocumentServiceTest {

    private DocumentService service;
    @Mock
    private DocumentDAO dao;
    private Locale fromLocale = new Locale(LocaleCode.EN, "English");
    private Locale toLocale = new Locale(LocaleCode.DE, "German");
    private String url = "http://example.com";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new DocumentService(dao);
    }

    @Test
    public void canGetByUrlIfInDatabase() {
        Document doc = new Document();
        when(dao.getByUrl(url, fromLocale, toLocale)).thenReturn(doc);

        Document result = service.getOrCreateByUrl(url, fromLocale, toLocale);
        assertThat(result).isSameAs(doc);
    }

    @Test
    public void canCreateNewDocIfNotInDatabase() {
        when(dao.getByUrl(url, fromLocale, toLocale)).thenReturn(null);
        ArgumentCaptor<Document> argCaptor =
                ArgumentCaptor.forClass(Document.class);
        // on persist we return the argument
        doAnswer(invocationOnMock -> invocationOnMock.getArguments()[0])
                .when(dao).persist(any(Document.class));

        Document result = service.getOrCreateByUrl(url, fromLocale, toLocale);

        verify(dao).persist(argCaptor.capture());
        assertThat(result).isSameAs(argCaptor.getValue());
        assertThat(result.getFromLocale()).isSameAs(fromLocale);
        assertThat(result.getToLocale()).isSameAs(toLocale);
        assertThat(result.getUrl()).isEqualTo(url);
    }

    @Test
    public void canGetByUrlAndOptionalOptions() {
        List<Document> expectedResult = Lists.newArrayList(new Document());
        when(dao.getByUrl(url, Optional.of(fromLocale.getLocaleCode()),
                Optional.of(toLocale.getLocaleCode()),
                Optional.empty())).thenReturn(
                expectedResult);

        List<Document> docs =
                service.getByUrl(url, Optional.of(fromLocale.getLocaleCode()),
                        Optional.of(toLocale.getLocaleCode()),
                        Optional.empty());

        verify(dao).getByUrl(url, Optional.of(fromLocale.getLocaleCode()),
                Optional.of(toLocale.getLocaleCode()),
                Optional.empty());

        assertThat(docs).isSameAs(expectedResult);
    }

    @Test
    public void canIncrementDocCount() {
        Document doc = new Document();
        final int oldCount = doc.getCount();
        doAnswer(invocationOnMock -> invocationOnMock.getArguments()[0])
                .when(dao).merge(any(Document.class));

        Document result = service.incrementDocRequestCount(doc);

        assertThat(result.getCount()).isEqualTo(oldCount + 1);
        verify(dao).merge(doc);
    }

}
