package org.magpie.mt.util;

import org.junit.Test;
import org.magpie.mt.api.dto.LocaleCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class OkapiUtilTest {

    @Test
    public void countEmptyWordsTest() {
        String test = "";
        long words = OkapiUtil.countWords(test, LocaleCode.EN.getId());
        assertThat(words).isEqualTo(0);
    }

    @Test
    public void countWordsTest() {
        String test = "Long long long time ago";
        long words = OkapiUtil.countWords(test, LocaleCode.EN.getId());
        assertThat(words).isEqualTo(5);
    }

    @Test
    public void countWordsTest2() {
        String test = "3 words sentence";
        long words = OkapiUtil.countWords(test, LocaleCode.EN.getId());
        assertThat(words).isEqualTo(3);
    }
}
