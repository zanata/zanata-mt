package org.zanata.mt.dao;

import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.mt.JPATest;
import org.zanata.mt.api.dto.LocaleCode;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;
import org.zanata.mt.service.DateRange;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiRunner.class)
public class DocumentDAOTest extends JPATest {

    private DocumentDAO dao;

    private Locale fromLocale = new Locale(LocaleCode.EN_US, "English US");
    private Locale toLocale = new Locale(LocaleCode.DE, "German");
    private Locale toLocale2 = new Locale(LocaleCode.FR, "French");

    @Before
    public void setup() {
        dao = new DocumentDAO(getEm());
    }

    @Test
    public void testEmptyConstructor() {
        DocumentDAO dao = new DocumentDAO();
    }

    @Test
    public void testGetByUrlNoMatchUrl() {
        Document doc = dao.getByUrl("http://localhost3", fromLocale, toLocale);
        assertThat(doc).isNull();
    }

    @Test
    public void testGetByUrl() {
        Document doc = dao.getByUrl("http://localhost", fromLocale, toLocale);
        assertThat(doc).isNotNull();

        doc = dao.getByUrl("http://localhost2", fromLocale, toLocale);
        assertThat(doc).isNotNull();
    }

    @Test
    public void testGetListByUrlAll() {
        List<Document> docs =
                dao.getByUrl("http://localhost", Optional.empty(),
                        Optional.empty(), Optional.empty());
        assertThat(docs).hasSize(3);
    }

    @Test
    public void testGetListByUrlOldRecord() {
        Optional<DateRange> dateRange =
                Optional.of(DateRange.from("2000-01-01..2000-01-01"));

        List<Document> docs =
                dao.getByUrl("http://localhost", Optional.empty(),
                        Optional.empty(), dateRange);
        assertThat(docs).isEmpty();
    }

    @Test
    public void testGetListByUrlWithSourceLocale() {
        List<Document> docs =
                dao.getByUrl("http://localhost",
                        Optional.of(fromLocale.getLocaleCode()),
                        Optional.empty(), Optional.empty());
        assertThat(docs).hasSize(2);
    }

    @Test
    public void testGetListByUrlWithSourceAndTargetLocale() {
        List<Document> docs =
                dao.getByUrl("http://localhost",
                        Optional.of(fromLocale.getLocaleCode()),
                        Optional.of(toLocale.getLocaleCode()), Optional.empty());
        assertThat(docs).hasSize(1);
    }

    @Test
    public void testGetOrCreateByUrl() {
        Document doc = dao.getOrCreateByUrl("http://localhost3", fromLocale,
                toLocale);
        assertThat(doc).isNotNull();
    }

    @Test
    public void testGetUrlListAll() throws Exception {
        List<String> urls = dao.getUrlList(Optional.empty());
        assertThat(urls).hasSize(2);
    }

    @Test
    public void testGetUrlListOldRecord() throws Exception {
        String dateRangeParam = "2000-01-01..2000-01-01";
        Optional<DateRange> dateParam =
                Optional.ofNullable(DateRange.from(dateRangeParam));

        List<String> urls = dao.getUrlList(dateParam);
        assertThat(urls).isEmpty();
    }

    @Test
    public void testOnPersist() {
        Document document = new Document("http://localhost3", fromLocale,
                toLocale);
        Date creationDate = document.getCreationDate();
        Date lastChanged = document.getLastChanged();

        document = dao.persist(document);
        assertThat(document.getCreationDate()).isNotNull()
                .isNotEqualTo(creationDate);
        assertThat(document.getLastChanged()).isNotNull()
                .isNotEqualTo(lastChanged);
    }

    @Test
    public void testPreUpdate() {
        Document doc = dao.getByUrl("http://localhost", fromLocale, toLocale);
        Date lastChanged = doc.getLastChanged();
        doc.incrementCount();
        dao.persist(doc);
        dao.flush();
        doc = dao.getByUrl("http://localhost", fromLocale, toLocale);
        assertThat(doc.getLastChanged()).isNotNull()
                .isNotEqualTo(lastChanged);
        assertThat(doc.getCount()).isEqualTo(1);
    }

    @Override
    protected void setupTestData() {
        getEm().persist(fromLocale);
        getEm().persist(toLocale);
        getEm().persist(toLocale2);

        Document document = new Document("http://localhost", fromLocale,
                toLocale);
        Document document2 = new Document("http://localhost", fromLocale,
                toLocale2);
        Document document3 = new Document("http://localhost", toLocale2,
                fromLocale);
        Document document4 = new Document("http://localhost2", fromLocale,
                toLocale);
        getEm().persist(document);
        getEm().persist(document2);
        getEm().persist(document3);
        getEm().persist(document4);
    }
}
