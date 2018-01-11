package org.zanata.magpie.process;

import org.junit.Test;
import org.zanata.magpie.api.dto.LocaleCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocumentProcessKeyTest {

    @Test
    public void testToString() {
        DocumentProcessKey key =
                new DocumentProcessKey("url1", LocaleCode.EN, LocaleCode.DE);
        assertThat(key.toString())
                .contains("url1", LocaleCode.EN.getId(), LocaleCode.DE.getId());
    }

    @Test
    public void testEqualsAndHashCode() {
        DocumentProcessKey key =
                new DocumentProcessKey("url1", LocaleCode.EN, LocaleCode.DE);

        DocumentProcessKey diffUrl =
                new DocumentProcessKey("url2", LocaleCode.EN, LocaleCode.DE);

        assertThat(key.equals(diffUrl)).isFalse();
        assertThat(key.hashCode()).isNotEqualTo(diffUrl.hashCode());

        DocumentProcessKey diffSrcLocale =
                new DocumentProcessKey("url1", LocaleCode.EN_US, LocaleCode.DE);

        assertThat(key.equals(diffSrcLocale)).isFalse();
        assertThat(key.hashCode()).isNotEqualTo(diffSrcLocale.hashCode());

        DocumentProcessKey diffTransLocale =
                new DocumentProcessKey("url1", LocaleCode.EN, LocaleCode.FR);

        assertThat(key.equals(diffTransLocale)).isFalse();
        assertThat(key.hashCode()).isNotEqualTo(diffTransLocale.hashCode());
    }
}
