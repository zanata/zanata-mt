package org.magpie.mt.backend.ms.internal.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSTranslateArrayReqOptionsTest {

    @Test
    public void testConstructor() {
        MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
    }

    @Test
    public void testCategory() {
        MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
        options.setCategory("category");
        assertThat(options.getCategory()).isEqualTo("category");
    }

    @Test
    public void testContentType() {
        MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
        options.setContentType("contentType");
        assertThat(options.getContentType()).isEqualTo("contentType");
    }

    @Test
    public void testReservedFlags() {
        MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
        options.setReservedFlags("reservedFlags");
        assertThat(options.getReservedFlags()).isEqualTo("reservedFlags");
    }

    @Test
    public void testState() {
        MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
        options.setState(1);
        assertThat(options.getState()).isEqualTo(1);
    }

    @Test
    public void testUri() {
        MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
        options.setUri("uri");
        assertThat(options.getUri()).isEqualTo("uri");
    }

    @Test
    public void testUser() {
        MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
        options.setUser("user");
        assertThat(options.getUser()).isEqualTo("user");
    }

    @Test
    public void testEqualsAndHashcode() {
        MSTranslateArrayReqOptions options1 = getDefaultOptions();
        MSTranslateArrayReqOptions options2 = getDefaultOptions();
        assertThat(options1.hashCode()).isEqualTo(options2.hashCode());
        assertThat(options1.equals(options2)).isTrue();

        // change category
        options2 = getDefaultOptions();
        options2.setCategory("category2");
        assertThat(options1.hashCode()).isNotEqualTo(options2.hashCode());
        assertThat(options1.equals(options2)).isFalse();

        // change content type
        options2 = getDefaultOptions();
        options2.setContentType("contentType2");
        assertThat(options1.hashCode()).isNotEqualTo(options2.hashCode());
        assertThat(options1.equals(options2)).isFalse();

        // change reserved flags
        options2 = getDefaultOptions();
        options2.setReservedFlags("reserved");
        assertThat(options1.hashCode()).isNotEqualTo(options2.hashCode());
        assertThat(options1.equals(options2)).isFalse();

        // change state
        options2 = getDefaultOptions();
        options2.setState(2);
        assertThat(options1.hashCode()).isNotEqualTo(options2.hashCode());
        assertThat(options1.equals(options2)).isFalse();

        // change uri
        options2 = getDefaultOptions();
        options2.setUri("uri2");
        assertThat(options1.hashCode()).isNotEqualTo(options2.hashCode());
        assertThat(options1.equals(options2)).isFalse();

        // change user
        options2 = getDefaultOptions();
        options2.setUser("user2");
        assertThat(options1.hashCode()).isNotEqualTo(options2.hashCode());
        assertThat(options1.equals(options2)).isFalse();
    }

    private MSTranslateArrayReqOptions getDefaultOptions() {
        MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
        options.setCategory("category");
        options.setContentType("contentType");
        options.setReservedFlags("reservedFlags");
        options.setState(1);
        options.setUri("uri");
        options.setUser("user");

        return options;
    }


}
