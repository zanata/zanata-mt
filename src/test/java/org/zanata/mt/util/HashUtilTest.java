package org.zanata.mt.util;

import org.junit.Test;
import org.zanata.mt.api.dto.LocaleId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class HashUtilTest {
    @Test
    public void testGenerateHash() {
        String test = "testing";
        LocaleId localeId = LocaleId.DE;
        String expectedHash = "78abe7884fa62f37e99113e843c1949a";

        String hash = HashUtil.generateHash(test, localeId);
        assertThat(hash).isEqualTo(expectedHash);
    }
}
