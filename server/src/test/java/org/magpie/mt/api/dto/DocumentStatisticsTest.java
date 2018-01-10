package org.magpie.mt.api.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocumentStatisticsTest {

    @Test
    public void testEmptyConstructor() {
        DocumentStatistics stats = new DocumentStatistics();
    }

    @Test
    public void testConstructor() {
        String url = "http://localhost";
        DocumentStatistics stats = new DocumentStatistics(url);
        assertThat(stats.getUrl()).isEqualTo(url);
    }

    @Test
    public void testAddRequestCount() {
        String url = "http://localhost";
        DocumentStatistics stats = new DocumentStatistics(url);
        assertThat(stats.getRequestCounts()).hasSize(0);

        int count = 1;
        TranslationRequestStatistics requestStats =
                new TranslationRequestStatistics(LocaleCode.EN_US.getId(),
                        LocaleCode.FR.getId(), count, 10);

        stats.addRequestCount(requestStats.getFromLocaleCode(),
                requestStats.getToLocaleCode(), count,
                requestStats.getWordCount());

        assertThat(stats.getRequestCounts()).hasSize(1)
                .contains(requestStats);
    }

    @Test
    public void testUpdateRequestCount() {
        String url = "http://localhost";
        DocumentStatistics stats = new DocumentStatistics(url);

        int count = 1;
        TranslationRequestStatistics requestStats =
                new TranslationRequestStatistics(LocaleCode.EN_US.getId(),
                        LocaleCode.FR.getId(), count, 10);

        stats.addRequestCount(requestStats.getFromLocaleCode(),
                requestStats.getToLocaleCode(), count,
                requestStats.getWordCount());
        assertThat(stats.getRequestCounts()).hasSize(1);
        assertThat(stats.getRequestCounts().get(0).getCount()).isEqualTo(count);
        assertThat(stats.getRequestCounts().get(0).getWordCount())
                .isEqualTo(requestStats.getWordCount());

        stats.addRequestCount(requestStats.getFromLocaleCode(),
                requestStats.getToLocaleCode(), count,
                requestStats.getWordCount());
        assertThat(stats.getRequestCounts()).hasSize(1);
        assertThat(stats.getRequestCounts().get(0).getCount()).isEqualTo(2);
    }

    @Test
    public void testEqualsAndHashCode() {
        String url = "http://localhost";
        String url2 = "http://localhost2";
        DocumentStatistics stats = new DocumentStatistics(url);
        DocumentStatistics stats2 = new DocumentStatistics(url);

        assertThat(stats.equals(stats2)).isTrue();
        assertThat(stats.hashCode()).isEqualTo(stats2.hashCode());

        stats.addRequestCount(LocaleCode.EN_US.getId(), LocaleCode.FR.getId(),
                1, 1);
        assertThat(stats.equals(stats2)).isFalse();
        assertThat(stats.hashCode()).isNotEqualTo(stats2.hashCode());

        stats = new DocumentStatistics(url);
        stats2 = new DocumentStatistics(url2);
        assertThat(stats.equals(stats2)).isFalse();
        assertThat(stats.hashCode()).isNotEqualTo(stats2.hashCode());
    }
}
