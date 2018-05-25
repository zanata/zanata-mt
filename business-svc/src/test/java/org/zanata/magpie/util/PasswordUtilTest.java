package org.zanata.magpie.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class PasswordUtilTest {

    private PasswordUtil passwordUtil;

    @Before
    public void setUp() {
        passwordUtil = new PasswordUtil();
    }

    @Test
    public void canGenerateRandomPassword() {
        String randomPassword1 = passwordUtil.generateRandomPassword(16);
        String randomPassword2 = passwordUtil.generateRandomPassword(16);

        assertThat(randomPassword1).hasSize(16);
        assertThat(randomPassword2).hasSize(16);
        assertThat(randomPassword1).isNotEqualTo(randomPassword2);
    }

}
