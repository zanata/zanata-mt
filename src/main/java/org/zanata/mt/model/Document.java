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
    @JoinColumn(name = "fromLocaleId", nullable = false, updatable = false)
    private Locale fromLocale;

    @NotNull
    @NaturalId
    @ManyToOne(optional = false)
    @JoinColumn(name = "toLocaleId", nullable = false, updatable = false)
    private Locale toLocale;

    private int count;

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

    public Document(String url, Locale fromLocale, Locale toLocale) {
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.fromLocale = fromLocale;
        this.toLocale = toLocale;
        updateUrlHash();
    }

    public Document(String url, Locale fromLocale, Locale toLocale,
            Set<TextFlow> textFlows) {
        this(url, fromLocale, toLocale);
        this.textFlows = textFlows;
    }

    private void updateUrlHash() {
        this.urlHash = HashUtil.generateHash(url);
    }

    public String getUrl() {
        return url;
    }

    public Locale getFromLocale() {
        return fromLocale;
    }

    public Locale getToLocale() {
        return toLocale;
    }

    public int getCount() {
        return count;
    }

    public String getUrlHash() {
        return urlHash;
    }

    public Set<TextFlow> getTextFlows() {
        return textFlows;
    }

    public void incrementCount() {
        this.count += 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document)) return false;

        Document document = (Document) o;

        if (getUrlHash() != null ? !getUrlHash().equals(document.getUrlHash()) :
                document.getUrlHash() != null) return false;
        if (getFromLocale() != null ?
                !getFromLocale().equals(document.getFromLocale()) :
                document.getFromLocale() != null) return false;
        return getToLocale() != null ?
                getToLocale().equals(document.getToLocale()) :
                document.getToLocale() == null;
    }

    @Override
    public int hashCode() {
        int result = getUrlHash() != null ? getUrlHash().hashCode() : 0;
        result =
                31 * result +
                        (getFromLocale() != null ? getFromLocale().hashCode() :
                                0);
        result = 31 * result +
                (getToLocale() != null ? getToLocale().hashCode() : 0);
        return result;
    }
}
