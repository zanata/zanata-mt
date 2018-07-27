package org.zanata.magpie.util;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.zanata.magpie.api.dto.LocaleCode;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class SegmentString {

    /**
     * Segment a paragraph into sentences
     */
    public static List<String> segmentString(@NotNull String text,
            Optional<LocaleCode> localeCode) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }

        Locale locale =
                localeCode.isPresent() ? new Locale(localeCode.get().getId()) :
                        Locale.getDefault();
        BreakIterator boundary = BreakIterator.getSentenceInstance(locale);
        boundary.setText(text);
        return extractList(boundary, text);
    }

    private static List<String> extractList(BreakIterator boundary,
            String source) {
        List<String> strings = new ArrayList<>();
        int start = boundary.first();
        for (int end = boundary.next();
                end != BreakIterator.DONE;
                start = end, end = boundary.next()) {
            strings.add(source.substring(start, end));
        }
        return strings;
    }
}
