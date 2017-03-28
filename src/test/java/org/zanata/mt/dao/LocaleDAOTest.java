package org.zanata.mt.dao;

import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.mt.JPATest;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.model.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiRunner.class)
public class LocaleDAOTest extends JPATest {

    private LocaleDAO dao;

    @Before
    public void setup() {
        dao = new LocaleDAO(getEm());
    }

    @Test
    public void testEmptyConstructor() {
        LocaleDAO dao = new LocaleDAO();
    }

    @Test
    public void testGetByLocaleIdEmpty() {
        Locale locale = dao.getByLocaleId(LocaleId.FR);
        assertThat(locale).isNull();
    }

    @Test
    public void testGetByLocaleId() {
        Locale locale = dao.getByLocaleId(LocaleId.EN_US);
        assertThat(locale).isNotNull().extracting("localeId")
                .contains(LocaleId.EN_US);
    }

    @Test
    public void testGetOrCreateByLocaleId() {
        Locale locale = dao.generateLocale(LocaleId.EN_US);
        assertThat(locale).isNotNull().extracting("localeId")
                .contains(LocaleId.EN_US);
    }

    @Override
    protected void setupTestData() {
        getEm().persist(new Locale(LocaleId.EN_US, "English US"));
        getEm().persist(new Locale(LocaleId.DE, "German"));
    }
}
