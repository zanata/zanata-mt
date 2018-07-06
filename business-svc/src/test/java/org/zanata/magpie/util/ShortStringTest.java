package org.zanata.magpie.util;

import com.google.common.collect.Lists;
import liquibase.util.StringUtils;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ShortStringTest {

    @Test
    public void testShortenShortString() {
        String s1 = "string which is already short";
        String s2 = ShortString.shorten(s1);
        assertThat(s1).isEqualTo(s2);
    }

    @Test
    public void testShortenLongString() {
        String s1 =
                "string which is really quite long. string which is really quite long. string which is really quite long. string which is really quite long. string which is really quite long. ";
        String s2 = ShortString.shorten(s1);
        assertThat(s2.length() <= ShortString.MAX_LENGTH).isTrue();
        String s3 = ShortString.shorten(s2);
        assertThat(s2).isEqualTo(s3);
    }

    @Test
    public void testConfigurableShortenLongString() {
        int maximumLength = 15;
        String s1 =
                "string which is really quite long. string which is really quite long. string which is really quite long. string which is really quite long. string which is really quite long. ";
        String s2 = ShortString.shorten(s1, maximumLength);
        assertThat(s2.length() <= maximumLength).isTrue();
        String s3 = ShortString.shorten(s2, maximumLength);
        assertThat(s2).isEqualTo(s3);
    }

    @Test
    public void testShortenListOfLongString() {
        String s1 = StringUtils.repeat("t1", ShortString.MAX_LENGTH);
        String s2 = StringUtils.repeat("t2", ShortString.MAX_LENGTH);

        List<String> longStrings = Lists.newArrayList(s1, s2);
        List<String>
                shortStrings = ShortString.shorten(longStrings);

        assertThat(shortStrings.size()).isEqualTo(longStrings.size());

        for (String shortString: shortStrings) {
            assertThat(shortString.length()).isEqualTo(ShortString.MAX_LENGTH);
        }
    }
}
