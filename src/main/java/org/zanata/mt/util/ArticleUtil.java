package org.zanata.mt.util;

import com.google.common.collect.Lists;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Utility class for Article
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class ArticleUtil {
    static final String WRAPPER_ID = "ZanataMT-wrapper";

    @SuppressWarnings("unused")
    private ArticleUtil() {
    }

    /**
     * Check if element contains non-translatable node
     */
    public static boolean containsNonTranslatableNode(String html) {
        Elements elements = getNonTranslatableElements(html);
        return elements != null && !elements.isEmpty();
    }

    /**
     * Get html string for all non-translatable element
     */
    public static List<String> getNonTranslatableHtml(String html) {
        Elements elements = getNonTranslatableElements(html);
        List<String> results = Lists.newArrayList();
        for (Element element: elements) {
            results.add(element.html());
        }
        return results;
    }

    /**
     * Get all non-translatable elements
     */
    public static Elements getNonTranslatableElements(String html) {
        Document document = Jsoup.parse(html);
        return document.getElementsByAttributeValueContaining("translate",
                        "no");
    }

    /**
     * Check if element contains KCS article private notes
     *
     * section with id 'private-notes...'
     */
    public static boolean containsPrivateNotes(String html) {
        Document document = Jsoup.parse(html);
        Elements elements = document.select("[id^='private-notes']");
        return elements != null && !elements.isEmpty();
    }

    /**
     * Check if element contains KCS article code-raw section
     */
    public static boolean containsKCSCodeSection(String html) {
        Elements elements = getKCSCodeElements(html);
        return elements != null && !elements.isEmpty();
    }

    /**
     * Get html string for all kcs code section
     */
    public static List<String> getKCSCodeHtml(String html) {
        Elements elements = getKCSCodeElements(html);

        List<String> results = Lists.newArrayList();
        for (Element element: elements) {
            results.add(element.html());
        }
        return results;
    }

    /**
     * Get all KCS code elements
     */
    public static Elements getKCSCodeElements(String html) {
        Document document = Jsoup.parse(html);
        return document.select("#code-raw");
    }
}
