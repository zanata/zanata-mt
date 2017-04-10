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

    private final Node node;

    private Map<String, Node> placeholderIdMap = new HashMap<>();

    public TranslatableHTMLNode(@NotNull Node node,
            @NotNull Map<String, Node> placeholderIdMap) {
        this.node = node;
        this.placeholderIdMap = placeholderIdMap;
    }

    // Original nodes with placeholder id
    public Map<String, Node> getPlaceholderIdMap() {
        return placeholderIdMap;
    }

    // html string of {@link #nodes}
    public String getHtml() {
        return node.outerHtml();
    }
}
