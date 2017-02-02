package org.zanata.mt.util;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class PasswordUtilTest {

    @Test
    public void testGeneratePasswordEmpty() {
        String username = "";
        String pass = PasswordUtil.createPasswordKey(username);
        assertThat(pass).isNull();
    }

    @Test
    public void testGeneratePassword() {
        String username = "user1";
        String pass = PasswordUtil.createPasswordKey(username);
        assertThat(pass).isNotNull();
    }
}
