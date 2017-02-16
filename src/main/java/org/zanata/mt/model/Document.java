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
        if (!url.equals(document.url)) return false;

        if (!srcLocale.equals(document.srcLocale)) return false;
        return targetLocale.equals(document.targetLocale);
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + srcLocale.hashCode();
        result = 31 * result + targetLocale.hashCode();
        return result;
    }
}
