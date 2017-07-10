package org.zanata.mt.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class HashUtilTest {
    @Test
    public void testGenerateHash() {
        String test = "testing";
        String expectedHash = "ae2b1fca515949e5d54fb22b8ed95575";

        String hash = HashUtil.generateHash(test);
        assertThat(hash).isEqualTo(expectedHash);
    }
}
