package org.zanata.mt.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class DomUtil {
    /**
     * Parse html and return as Jsoup elements
     *
     * @param html - html to parse
     */
    public static Elements parseAsElement(String html) {
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        return body != null && !body.children().isEmpty()
            ? body.children() : null;
    }

    public static String extractBodyContentHTML(Document doc) {
        return doc.body() != null ? doc.body().html() : "";
    }
}
