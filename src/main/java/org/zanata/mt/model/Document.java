package org.zanata.mt.model;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.zanata.mt.util.HashUtil;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
public class Document extends ModelEntity {

    private static final long serialVersionUID = -3394088546058798299L;

    @URL
    @NotNull
    private String url;

    @NotNull
    @NaturalId
    @ManyToOne(optional = false)
    @JoinColumn(name = "srcLocaleId", nullable = false, updatable = false)
    private Locale srcLocale;

    @NotNull
    @NaturalId
    @ManyToOne(optional = false)
    @JoinColumn(name = "targetLocaleId", nullable = false, updatable = false)
    private Locale targetLocale;

    private int usedCount;

    @NotEmpty
    @Size(max = 255)
    @NaturalId
    private String urlHash;

    @ManyToMany
    @JoinTable(name = "Document_TextFlow",
            joinColumns = @JoinColumn(name = "documentId"),
            inverseJoinColumns = @JoinColumn(name = "textFlowId"))
    private Set<TextFlow> textFlows = new HashSet<>();

    public Document() {
    }

    public Document(String url, Locale srcLocale, Locale targetLocale) {
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.srcLocale = srcLocale;
        this.targetLocale = targetLocale;
        updateUrlHash();
    }

    public Document(String url, Locale srcLocale, Locale targetLocale,
            Set<TextFlow> textFlows) {
        this(url, srcLocale, targetLocale);
        this.textFlows = textFlows;
    }

    private void updateUrlHash() {
        this.urlHash = HashUtil.generateHash(url);
    }

    public String getUrl() {
        return url;
    }

    public Locale getSrcLocale() {
        return srcLocale;
    }

    public Locale getTargetLocale() {
        return targetLocale;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public String getUrlHash() {
        return urlHash;
    }

    public Set<TextFlow> getTextFlows() {
        return textFlows;
    }

    public void incrementUsedCount() {
        this.usedCount += 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document)) return false;

        Document document = (Document) o;

        if (getUrlHash() != null ? !getUrlHash().equals(document.getUrlHash()) :
                document.getUrlHash() != null) return false;
        if (getSrcLocale() != null ?
                !getSrcLocale().equals(document.getSrcLocale()) :
                document.getSrcLocale() != null) return false;
        return getTargetLocale() != null ?
                getTargetLocale().equals(document.getTargetLocale()) :
                document.getTargetLocale() == null;
    }

    @Override
    public int hashCode() {
        int result = getUrlHash() != null ? getUrlHash().hashCode() : 0;
        result =
                31 * result +
                        (getSrcLocale() != null ? getSrcLocale().hashCode() :
                                0);
        result = 31 * result +
                (getTargetLocale() != null ? getTargetLocale().hashCode() : 0);
        return result;
    }
}
