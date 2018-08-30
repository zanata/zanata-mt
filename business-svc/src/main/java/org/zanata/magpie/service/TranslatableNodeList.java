package org.zanata.magpie.service;

import org.jsoup.nodes.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A holder object for html/xml in which all non-translatable jsoup nodes are being replaced
 * by placeholder.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslatableNodeList {

    private final List<Node> nodes;

    private final Map<String, Node> placeholderIdMap;

    public TranslatableNodeList(@NotNull List<Node> nodes,
            @NotNull Map<String, Node> placeholderIdMap) {
        this.nodes = nodes;
        this.placeholderIdMap = placeholderIdMap;
    }

    // Original nodes with placeholder id
    public Map<String, Node> getPlaceholderIdMap() {
        return placeholderIdMap;
    }

    // html/xml string of {@link #nodes}
    public String getHtml() {
        return nodes.stream().map(Node::outerHtml)
                .collect(Collectors.joining());
    }
}
