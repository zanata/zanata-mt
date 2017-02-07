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
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.service.ArticleConverter;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.zanata.mt.article.kcs.KCSUtil.generateCodeElementName;
import static org.zanata.mt.article.kcs.KCSUtil.generateNonTranslatableNode;
import static org.zanata.mt.article.kcs.KCSUtil.getRawCodePreElements;
import static org.zanata.mt.article.kcs.KCSUtil.isPrivateNotes;

/**
 * Extract KCS article for translation
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class KCSArticleConverter implements ArticleConverter {

    @Override
    public ArticleContents extractArticle(String html) {
        Document document = Jsoup.parse(html);

        List<ArticleNode> nodes = newArrayList();
        Map<String, Element> nonTranslatableElements = newHashMap();

        extractArticleHeader(document, nodes);
        preprocessArticleBody(document, nodes, nonTranslatableElements);

        return new ArticleContents(document, nodes, nonTranslatableElements);
    }

    @Override
    public void insertAttribution(ArticleContents articleContents,
            String html) throws ZanataMTException {
        KCSUtil.insertAttribution(articleContents.getDocument(), html);
    }

    // Extracts translatable headings as ArticleNodes to the list 'nodes'
    private void extractArticleHeader(Document document,
            List<ArticleNode> nodes) {
        Element header = KCSUtil.getHeader(document);
        if (header != null) {
            Element solutionTitle = header.select("h1.title").first();
            Element solutionStatus = header.select("span.status").first();

            nodes.add(new ArticleNode(solutionTitle));
            nodes.add(new ArticleNode(solutionStatus));
        }
    }

    // Modifies body of article by replacing non-translatable elements with placeholders.
    // Translatable sections are added as ArticleNodes to the list 'nodes'.
    // Original non-translatable elements within sections are stored in
    // 'nonTranslatableElements' for postprocessing after translation.
    private List<ArticleNode> preprocessArticleBody(Document document,
            List<ArticleNode> nodes, Map<String, Element> nonTranslatableElements) {
        Elements sections = document.getElementsByTag("section");

        int eleIndex = 0;
        for (Element section : sections) {
            // section with id 'private-notes...' is non-translatable
            if (isPrivateNotes(section)) {
                continue;
            }
            // replace pre elements with non-translatable placeholders
            Elements codeElements = getRawCodePreElements(section);
            for (Element element : codeElements) {
                String name = generateCodeElementName(eleIndex);
                nonTranslatableElements.put(name, element.clone());
                element.replaceWith(generateNonTranslatableNode(name));
                eleIndex++;
            }
            nodes.addAll(section.children().stream()
                .map((element -> new ArticleNode(element)))
                .collect(Collectors.toList()));
        }
        return nodes;
    }
}
