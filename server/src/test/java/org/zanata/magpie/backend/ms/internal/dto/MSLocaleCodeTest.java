package org.zanata.magpie.backend.ms.internal.dto;

import org.junit.Test;
import org.zanata.magpie.api.dto.LocaleCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSLocaleCodeTest {

    @Test
    public void testConstructorLocaleCode() {
        LocaleCode testLocale = LocaleCode.EN_US;
        MSLocaleCode localeCode = new MSLocaleCode(testLocale);
        assertThat(localeCode.getLocaleCode()).isEqualTo(testLocale.getId());
    }

    @Test
    public void testConstructorString() {
        String testLocale = "en-us";
        MSLocaleCode localeCode = new MSLocaleCode(testLocale);
        assertThat(localeCode.getLocaleCode()).isEqualTo(testLocale);
    }

    @Test
    public void testEqualsAndHash() {
        MSLocaleCode from = new MSLocaleCode(LocaleCode.EN_US);
        MSLocaleCode to = new MSLocaleCode(LocaleCode.EN_US);

        assertThat(from.equals(to)).isTrue();
        assertThat(from.hashCode()).isEqualTo(to.hashCode());

        to = new MSLocaleCode(LocaleCode.DE);

        assertThat(from.equals(to)).isFalse();
        assertThat(from.hashCode()).isNotEqualTo(to.hashCode());
    }
}
