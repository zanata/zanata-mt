package org.zanata.mt.util;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class UrlUtilTest {

    @Test
    public void testIsValidUrl() {
        String url = "http://localhost:8080";
        assertThat(UrlUtil.isValidURL(url)).isTrue();
    }

    @Test
    public void testIsInvalidUrl() {
        String url = "localhost:8080";
        assertThat(UrlUtil.isValidURL(url)).isFalse();
    }

    @Test
    public void testIsInvalidUrl2() {
        String url = "http://";
        assertThat(UrlUtil.isValidURL(url)).isFalse();
    }
}
