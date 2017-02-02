package org.zanata.mt.util;

import javax.annotation.Nullable;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class for Password
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class PasswordUtil {

    /**
     * Generate password with sha512Hex using username
     */
    public static @Nullable String createPasswordKey(@Nullable String username) {
        if (StringUtils.isNotBlank(username)) {
            return DigestUtils.sha512Hex(username);
        }
        return null;
    }
}
