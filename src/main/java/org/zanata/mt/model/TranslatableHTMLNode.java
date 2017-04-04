package org.zanata.mt.model;

import org.jsoup.nodes.*;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * A holder object for html which all non-translatable nodes are being replaced
 * by placeholder.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslatableHTMLNode {

    private final Element doc;

    private Map<String, Element> placeholderIdMap = new HashMap<>();

    public TranslatableHTMLNode(@NotNull Element doc,
            @NotNull Map<String, Element> placeholderIdMap) {
        this.doc = doc;
        this.placeholderIdMap = placeholderIdMap;
    }

    // Original nodes with placeholder id
    public Map<String, Element> getPlaceholderIdMap() {
        return placeholderIdMap;
    }

    // html string of {@link #doc}
    public String getHtml() {
        return doc.outerHtml();
    }
}
