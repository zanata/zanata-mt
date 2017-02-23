package org.zanata.mt.dao;

import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.zanata.mt.JPATest;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;

import javax.persistence.EntityManager;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiRunner.class)
public class DocumentDAOTest extends JPATest {

    private DocumentDAO dao;

    private Locale srcLocale = new Locale(LocaleId.EN_US, "English US");
    private Locale transLocale = new Locale(LocaleId.DE, "German");

    @Before
    public void setup() {
        dao = new DocumentDAO(getEm());
    }

    @Test
    public void testEmptyConstructor() {
        DocumentDAO dao = new DocumentDAO();
    }

    @Test
    public void testGetByUrlNull() {
        Document doc = dao.getByUrl("http://localhost3", srcLocale, transLocale);
        assertThat(doc).isNull();
    }

    @Test
    public void testGetByUrl() {
        Document doc = dao.getByUrl("http://localhost", srcLocale, transLocale);
        assertThat(doc).isNotNull();

        doc = dao.getByUrl("http://localhost2", srcLocale, transLocale);
        assertThat(doc).isNotNull();
    }

    @Test
    public void testGetOrCreateByUrl() {
        Document doc = dao.getOrCreateByUrl("http://localhost3", srcLocale, transLocale);
        assertThat(doc).isNotNull();
    }

    @Test
    public void testOnPersist() {
        Document document = new Document("http://localhost3", srcLocale, transLocale);
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
        Document doc = dao.getByUrl("http://localhost", srcLocale, transLocale);
        Date lastChanged = doc.getLastChanged();
        doc.incrementUsedCount();
        dao.persist(doc);
        dao.flush();
        doc = dao.getByUrl("http://localhost", srcLocale, transLocale);
        assertThat(doc.getLastChanged()).isNotNull()
                .isNotEqualTo(lastChanged);
    }

    @Override
    protected void setupTestData() {
        getEm().persist(srcLocale);
        getEm().persist(transLocale);

        Document document = new Document("http://localhost", srcLocale, transLocale);
        Document document2 = new Document("http://localhost2", srcLocale, transLocale);
        getEm().persist(document);
        getEm().persist(document2);
    }
}
