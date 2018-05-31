package org.zanata.magpie.util;

import org.junit.Test;
import org.zanata.magpie.api.dto.LocaleCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class CountUtilTest {

    @Test
    public void countEmptyWordsTest() {
        String test = "";
        long words = CountUtil.countWords(test, LocaleCode.EN.getId());
        assertThat(words).isEqualTo(0);
    }

    @Test
    public void countWordsTest() {
        String test = "Long long long time ago";
        long words = CountUtil.countWords(test, LocaleCode.EN.getId());
        assertThat(words).isEqualTo(5);
    }

    @Test
    public void countWordsTest2() {
        String test = "3 words sentence";
        long words = CountUtil.countWords(test, LocaleCode.EN.getId());
        assertThat(words).isEqualTo(3);
    }

    @Test
    public void countChars() {
        String test = "你好吗？";
        String test1 = "Thîs lóo̰ks we̐ird!";

        assertThat(CountUtil.countCharacters(test)).isEqualTo(4);
        assertThat(CountUtil.countCharacters(test1)).isEqualTo(21);

    }
}
