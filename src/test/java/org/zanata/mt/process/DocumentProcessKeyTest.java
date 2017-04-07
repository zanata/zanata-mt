package org.zanata.mt.process;

import org.junit.Test;
import org.zanata.mt.api.dto.LocaleId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocumentProcessKeyTest {
    @Test
    public void testEqualsAndHashCode() {
        DocumentProcessKey key =
                new DocumentProcessKey("url1", LocaleId.EN, LocaleId.DE);

        DocumentProcessKey diffUrl =
                new DocumentProcessKey("url2", LocaleId.EN, LocaleId.DE);

        assertThat(key.equals(diffUrl)).isFalse();
        assertThat(key.hashCode()).isNotEqualTo(diffUrl.hashCode());

        DocumentProcessKey diffSrcLocale =
                new DocumentProcessKey("url1", LocaleId.EN_US, LocaleId.DE);

        assertThat(key.equals(diffSrcLocale)).isFalse();
        assertThat(key.hashCode()).isNotEqualTo(diffSrcLocale.hashCode());

        DocumentProcessKey diffTransLocale =
                new DocumentProcessKey("url1", LocaleId.EN, LocaleId.FR);

        assertThat(key.equals(diffTransLocale)).isFalse();
        assertThat(key.hashCode()).isNotEqualTo(diffTransLocale.hashCode());
    }
}
