package org.magpie.mt.util;

import org.junit.Test;
import org.magpie.mt.api.dto.LocaleCode;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class SegmentStringTest {

    @Test
    public void segmentStringDefaultLocaleTest() {
        String text =
                "A cooled coincidence parks the spectacular. Behind a dragon suspects the roman fashion. How can the inhabited wallet provision her made journalist? The opera squashes the younger advantage.";
        List<String> strings = SegmentString.segmentString(text, Optional.empty());
        assertThat(strings.size()).isEqualTo(4);
    }

    @Test
    public void segmentStringTest() {
        String text =
                "Das nennen Sie ein Schinken-Sandwich? Nehmen Sie das sofort weg!";
        List<String> strings = SegmentString.segmentString(text, Optional.of(
                LocaleCode.DE));
        assertThat(strings.size()).isEqualTo(2);
    }
}
