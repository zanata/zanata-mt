package org.zanata.mt.article.kcs;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zanata.mt.article.ArticleContents;
import org.zanata.mt.article.ArticleNode;
import org.zanata.mt.service.ArticleConverter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Extract KCS article for translation
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class KCSArticleConverter implements ArticleConverter {

    public ArticleContents extractArticle(String html) {
        Document document = Jsoup.parse(html);

        List<ArticleNode> nodes = Lists.newArrayList();
        Map<String, ArticleNode> ignoreNodeMap = Maps.newHashMap();

        extractArticleHeader(document, nodes);
        extractArticleBody(document, nodes, ignoreNodeMap);

        return new ArticleContents(document, nodes, ignoreNodeMap);
    }

    private void extractArticleHeader(Document document,
            List<ArticleNode> nodes) {
        Elements header = document.getElementsByTag("header");
        Element solutionTitle = header.select("h1.title").first();
        Element solutionStatus = header.select("span.status").first();

        nodes.add(new ArticleNode(solutionTitle));
        nodes.add(new ArticleNode(solutionStatus));
    }

    private List<ArticleNode> extractArticleBody(Document document,
            List<ArticleNode> nodes, Map<String, ArticleNode> ignoreNodeMap) {
        Elements sections = document.getElementsByTag("section");
        for (Element section : sections) {
            // section with id 'private-notes...' is non-translatable
            if (KCSUtil.isPrivateNotes(section)) {
                continue;
            }
            /**
             * replace pre element with non-translatable node as placeholder
             */
            Elements codeElements = KCSUtil.getRawCodePreElements(section);
            for (Element element : codeElements) {
                String name = KCSUtil.generateCodeElementName(
                        sections.indexOf(section),
                        codeElements.indexOf(element));
                ignoreNodeMap.put(name, new ArticleNode(element.clone()));
                element.replaceWith(
                        KCSUtil.generateNonTranslatableNode(name));
            }
            nodes.addAll(section.children().stream()
                .map((element -> new ArticleNode(element)))
                .collect(Collectors.toList()));
        }
        return nodes;
    }
}
