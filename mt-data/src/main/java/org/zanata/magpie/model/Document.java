package org.zanata.magpie.model;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.zanata.magpie.util.HashUtil;

import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

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

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "documentId", insertable = false, updatable = false)
    @MapKey(name = "contentHash")
    private Map<String, TextFlow> textFlows;

    public Document() {
    }

    public Document(String url, Locale fromLocale, Locale toLocale) {
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.fromLocale = fromLocale;
        this.toLocale = toLocale;
        updateUrlHash();
    }

    public Document(String url, Locale fromLocale, Locale toLocale,
            Map<String, TextFlow> textFlows) {
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

    public Map<String, TextFlow> getTextFlows() {
        if (textFlows == null) {
            textFlows = new HashMap<>();
        }
        return textFlows;
    }

    public void incrementCount() {
        this.count += 1;
    }

    @Override
    public boolean equals(@Nullable Object o) {
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
