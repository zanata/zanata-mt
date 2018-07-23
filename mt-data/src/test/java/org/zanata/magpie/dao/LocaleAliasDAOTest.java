package org.zanata.magpie.dao;

import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.magpie.JPATest;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.model.Locale;
import org.zanata.magpie.model.LocaleAlias;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RunWith(CdiRunner.class)
public class LocaleAliasDAOTest extends JPATest {

    private LocaleAliasDAO dao;

    @Before
    public void setup() {
        dao = new LocaleAliasDAO(getEm());
    }

    @Test
    public void emptyConstructor() {
        LocaleAliasDAO ignored = new LocaleAliasDAO();
    }

    @Test
    public void nonExistentAlias() {
        LocaleAlias alias = dao.getByLocaleCode(LocaleCode.FR);
        assertThat(alias).isNull();
    }

    @Test
    public void getByLocaleCode() {
        LocaleAlias alias = dao.getByLocaleCode(LocaleCode.EN);
        assertThat(alias)
                .isNotNull()
                .extracting("localeCode")
                .contains(LocaleCode.EN);
        assertThat(alias.getLocale())
                .isNotNull()
                .extracting("localeCode")
                .contains(LocaleCode.EN_US);
    }

    @Override
    protected void setupTestData() {
        Locale enUS = new Locale(LocaleCode.EN_US, "English US");
        getEm().persist(enUS);
        getEm().persist(new LocaleAlias(LocaleCode.EN, enUS));
    }
}
