package org.zanata.mt.api.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslationRequestStatisticsTest {

    @Test
    public void testConstructor() {
        TranslationRequestStatistics stats =
                new TranslationRequestStatistics();
    }

    @Test
    public void testConstructor2() {
        TranslationRequestStatistics stats =
                new TranslationRequestStatistics(LocaleCode.EN_US.getId(),
                        LocaleCode.FR.getId(), 0);
        assertThat(stats.getFromLocaleCode()).isEqualTo(LocaleCode.EN_US.getId());
        assertThat(stats.getToLocaleCode()).isEqualTo(LocaleCode.FR.getId());
        assertThat(stats.getCount()).isEqualTo(0);
    }

    @Test
    public void testAddCount() {
        TranslationRequestStatistics stats =
                new TranslationRequestStatistics(LocaleCode.EN_US.getId(),
                        LocaleCode.FR.getId(), 1);
        assertThat(stats.getCount()).isEqualTo(1);

        stats.addCount(10);
        assertThat(stats.getCount()).isEqualTo(11);
    }

    @Test
    public void testEqualsAndHashCode() {
        TranslationRequestStatistics stats =
                new TranslationRequestStatistics(LocaleCode.EN_US.getId(),
                        LocaleCode.FR.getId(), 1);
        TranslationRequestStatistics stats2 =
                new TranslationRequestStatistics(LocaleCode.EN_US.getId(),
                        LocaleCode.FR.getId(), 1);
        assertThat(stats.equals(stats2)).isTrue();
        assertThat(stats.hashCode()).isEqualTo(stats2.hashCode());

        // different toLocaleCode
        stats2 =
                new TranslationRequestStatistics(LocaleCode.EN.getId(),
                        LocaleCode.FR.getId(), 1);
        assertThat(stats.equals(stats2)).isFalse();
        assertThat(stats.hashCode()).isNotEqualTo(stats2.hashCode());

        // different fromLocaleCode
        stats2 =
                new TranslationRequestStatistics(LocaleCode.EN_US.getId(),
                        LocaleCode.DE.getId(), 1);
        assertThat(stats.equals(stats2)).isFalse();
        assertThat(stats.hashCode()).isNotEqualTo(stats2.hashCode());

        // different count
        stats2 =
                new TranslationRequestStatistics(LocaleCode.EN_US.getId(),
                        LocaleCode.DE.getId(), 2);
        assertThat(stats.equals(stats2)).isFalse();
        assertThat(stats.hashCode()).isNotEqualTo(stats2.hashCode());
    }
}
