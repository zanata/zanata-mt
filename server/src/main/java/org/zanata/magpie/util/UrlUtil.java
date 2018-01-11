package org.zanata.magpie.util;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class UrlUtil {

    @SuppressWarnings("unused")
    private UrlUtil() {
    }

    public static boolean isValidURL(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        try {
            URL u = new URL(url);
            u.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
        return true;
    }
}
