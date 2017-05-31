package org.zanata.mt.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.mt.api.dto.LocaleCode;
import org.zanata.mt.util.HashUtil;
import org.zanata.mt.util.OkapiUtil;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
public class TextFlow extends ModelEntity {
    private static final long serialVersionUID = -4550040877568062431L;

    @NaturalId
    @ManyToOne
    @JoinColumn(name = "documentId", updatable = false, nullable = false)
    @NotNull
    private Document document;

    @NotEmpty
    @Size(max = 255)
    @NaturalId
    private String contentHash;

    @NaturalId
    @ManyToOne(optional = false)
    @JoinColumn(name = "localeId", nullable = false, updatable = false)
    @NotNull
    private Locale locale;

    @NotEmpty
    private String content;

    @NotNull
    private Long wordCount;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "textFlow",
        fetch = FetchType.EAGER)
    private List<TextFlowTarget> targets = new ArrayList<>();

    public TextFlow() {
    }

    public TextFlow(Document document, String content, Locale locale) {
        this.content = content;
        this.locale = locale;
        this.document = document;
        updateContentHashAndWordCount();
    }

    public void setContent(String content) {
        this.content = content;
        updateContentHashAndWordCount();
    }

    private void updateContentHashAndWordCount() {
        this.contentHash = HashUtil.generateHash(content);
        String localeCode = LocaleCode.EN.getId();
        if (locale != null) {
            localeCode = locale.getLocaleCode().getId();
        }
        long count = OkapiUtil.countWords(content, localeCode);
        this.wordCount = count;
    }

    public String getContentHash() {
        return contentHash;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getContent() {
        return content;
    }

    public List<TextFlowTarget> getTargets() {
        return targets;
    }

    public Document getDocument() {
        return document;
    }

    public Long getWordCount() {
        return wordCount;
    }

    @Transient
    public List<TextFlowTarget> getTargetsByLocaleCode(LocaleCode localeCode) {
        return getTargets().stream()
                .filter(textFlowTarget -> textFlowTarget.getLocale()
                        .getLocaleCode().equals(localeCode))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextFlow textFlow = (TextFlow) o;

        if (document != null ? !document.equals(textFlow.document) :
                textFlow.document != null) return false;
        if (contentHash != null ? !contentHash.equals(textFlow.contentHash) :
                textFlow.contentHash != null) return false;
        if (locale != null ? !locale.equals(textFlow.locale) :
                textFlow.locale != null) return false;
        return wordCount != null ? wordCount.equals(textFlow.wordCount) :
                textFlow.wordCount == null;
    }

    @Override
    public int hashCode() {
        int result = document != null ? document.hashCode() : 0;
        result = 31 * result +
                (contentHash != null ? contentHash.hashCode() : 0);
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (wordCount != null ? wordCount.hashCode() : 0);
        return result;
    }
}
