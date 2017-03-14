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
    @SuppressWarnings("unused")
    private ArticleUtil() {
    }

    /**
     * Check if element contains non-translatable node
     */
    public static boolean containsNonTranslatableNode(String html) {
        Elements elements = getNonTranslatableElements(html);
        return !elements.isEmpty();
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
    public static @Nonnull Elements getNonTranslatableElements(String html) {
        Document document = Jsoup.parse(html);
        return document.getElementsByAttributeValueContaining("translate",
                        "no");
    }

    /**
     * Check if element contains KCS article private notes
     */
    public static boolean containsPrivateNotes(String html) {
        Elements elements = getPrivateNotesElements(html);
        return !elements.isEmpty();
    }

    /**
     * Get html string for all KCS article private notes
     */
    public static List<String> getPrivateNotesHtml(String html) {
        Elements elements = getPrivateNotesElements(html);
        List<String> results = Lists.newArrayList();
        for (Element element: elements) {
            results.add(element.html());
        }
        return results;
    }

    /**
     * Get all private-notes element
     *
     * Element with id 'private-notes*'
     */
    public static @Nonnull Elements getPrivateNotesElements(String html) {
        Document document = Jsoup.parse(html);
        return document.select("[id^='private-notes']");
    }

    /**
     * Check if element contains KCS article code-raw section
     */
    public static boolean containsKCSCodeSection(String html) {
        Elements elements = getKCSCodeElements(html);
        return !elements.isEmpty();
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
     *
     * Element with class=code-raw
     */
    public static @Nonnull Elements getKCSCodeElements(String html) {
        Document document = Jsoup.parse(html);
        return document.select(".code-raw");
    }
}
