package org.zanata.magpie.model;

import org.jsoup.nodes.*;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A holder object for html which all non-translatable nodes are being replaced
 * by placeholder.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslatableHTMLNode {

    private final List<Node> nodes;

    private Map<String, Node> placeholderIdMap = new HashMap<>();

    public TranslatableHTMLNode(@NotNull List<Node> nodes,
            @NotNull Map<String, Node> placeholderIdMap) {
        this.nodes = nodes;
        this.placeholderIdMap = placeholderIdMap;
    }

    // Original nodes with placeholder id
    public Map<String, Node> getPlaceholderIdMap() {
        return placeholderIdMap;
    }

    // html string of {@link #nodes}
    public String getHtml() {
        return nodes.stream().map(node -> node.outerHtml())
                .collect(Collectors.joining());
    }
}
