package org.zanata.mt.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class PasswordUtilTest {

    @Test
    public void testGenerateRandomHashWithSameUsername() {
        String username = "username";
        String key1 = PasswordUtil.createSaltedApiKey(username);
        String key2 = PasswordUtil.createSaltedApiKey(username);

        assertThat(key1).isNotEqualTo(key2);
    }
}
