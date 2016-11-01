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

    public void incrementCount() {
        this.usedCount += 1;
    }
}
