package org.magpie.mt.dao;

import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.magpie.mt.JPATest;
import org.magpie.mt.api.dto.LocaleCode;
import org.magpie.mt.model.Locale;

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
    public void testGetByLocaleCodeEmpty() {
        Locale locale = dao.getByLocaleCode(LocaleCode.FR);
        assertThat(locale).isNull();
    }

    @Test
    public void testGetByLocaleCode() {
        Locale locale = dao.getByLocaleCode(LocaleCode.EN_US);
        assertThat(locale).isNotNull().extracting("localeCode")
                .contains(LocaleCode.EN_US);
    }

    @Test
    public void testGetOrCreateByLocaleCodeNew() {
        Locale locale = dao.getOrCreateByLocaleCode(LocaleCode.FR);
        assertThat(locale).isNotNull().extracting("localeCode")
                .contains(LocaleCode.FR);
    }

    @Test
    public void testGetOrCreateByLocaleCode() {
        Locale locale = dao.getOrCreateByLocaleCode(LocaleCode.EN_US);
        assertThat(locale).isNotNull().extracting("localeCode")
                .contains(LocaleCode.EN_US);
    }

    @Override
    protected void setupTestData() {
        getEm().persist(new Locale(LocaleCode.EN_US, "English US"));
        getEm().persist(new Locale(LocaleCode.DE, "German"));
    }
}
