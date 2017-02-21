package org.zanata.mt.model;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
public class Document extends ModelEntity {

    private static final long serialVersionUID = -3394088546058798299L;

    @URL
    @NotNull
    @NaturalId
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

    public Document() {
        this(null, null, null);
    }

    public Document(String url, Locale srcLocale, Locale targetLocale) {
        this.url = url;
        this.srcLocale = srcLocale;
        this.targetLocale = targetLocale;
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

    public void incrementUsedCount() {
        this.usedCount += 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document)) return false;

        Document document = (Document) o;

        if (getUsedCount() != document.getUsedCount()) return false;
        if (getUrl() != null ? !getUrl().equals(document.getUrl()) :
                document.getUrl() != null) return false;
        if (getSrcLocale() != null ?
                !getSrcLocale().equals(document.getSrcLocale()) :
                document.getSrcLocale() != null) return false;
        return getTargetLocale() != null ?
                getTargetLocale().equals(document.getTargetLocale()) :
                document.getTargetLocale() == null;
    }

    @Override
    public int hashCode() {
        int result = getUrl() != null ? getUrl().hashCode() : 0;
        result =
                31 * result +
                        (getSrcLocale() != null ? getSrcLocale().hashCode() :
                                0);
        result = 31 * result +
                (getTargetLocale() != null ? getTargetLocale().hashCode() : 0);
        result = 31 * result + getUsedCount();
        return result;
    }
}
