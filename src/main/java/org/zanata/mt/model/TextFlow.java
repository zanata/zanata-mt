package org.zanata.mt.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.util.HashUtil;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
public class TextFlow extends ModelEntity {
    private static final long serialVersionUID = -4550040877568062431L;

    @ManyToMany
    @JoinTable(name = "Document_TextFlow",
            joinColumns = @JoinColumn(name = "textFlowId"),
            inverseJoinColumns = @JoinColumn(name = "documentId"))
    private Set<Document> documents = new HashSet<>();

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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "textFlow",
        fetch = FetchType.EAGER)
    private List<TextFlowTarget> targets = new ArrayList<>();

    public TextFlow() {
    }

    public TextFlow(Document document, String content, Locale locale) {
        this.content = content;
        this.locale = locale;
        documents.add(document);
        updateContentHash();
    }

    public void setContent(String content) {
        this.content = content;
        updateContentHash();
    }

    private void updateContentHash() {
        this.contentHash = HashUtil.generateHash(content);
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

    public Set<Document> getDocuments() {
        return documents;
    }

    @Transient
    public List<TextFlowTarget> getTargetsByLocaleId(LocaleId localeId) {
        return getTargets().stream()
                .filter(textFlowTarget -> textFlowTarget.getLocale()
                        .getLocaleId().equals(localeId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextFlow)) return false;

        TextFlow textFlow = (TextFlow) o;

        if (getContentHash() != null ? !getContentHash().equals(textFlow.getContentHash()) :
                textFlow.getContentHash() != null) return false;
        if (getLocale() != null ? !getLocale().equals(textFlow.getLocale()) :
                textFlow.getLocale() != null) return false;
        return getTargets() != null ?
                getTargets().equals(textFlow.getTargets()) :
                textFlow.getTargets() == null;
    }

    @Override
    public int hashCode() {
        int result = getContentHash() != null ? getContentHash().hashCode() : 0;
        result = 31 * result +
                (getLocale() != null ? getLocale().hashCode() : 0);
        result = 31 * result +
                (getTargets() != null ? getTargets().hashCode() : 0);
        return result;
    }
}
