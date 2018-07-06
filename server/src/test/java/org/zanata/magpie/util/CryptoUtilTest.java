package org.zanata.magpie.util;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class CryptoUtilTest {

    @Test
    public void testDecrypt() {
        String expectedValue = "testing";
        String passphrase = "/api/account/login/test";
        String encryptedValue = "U2FsdGVkX18yRsxRcAMD+FUvQ1OqXIoSpps96iVs/Ug=";

        String value = CryptoUtil.decrypt(passphrase, encryptedValue);
        assertThat(value).isEqualTo(expectedValue);
    }
}
