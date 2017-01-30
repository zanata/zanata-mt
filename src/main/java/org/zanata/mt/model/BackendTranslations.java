package org.zanata.mt.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BackendTranslations {
    private List<String> translations;
    private String attribution;

    public BackendTranslations(String translation,
            @Nullable String attribution) {
        this(Lists.newArrayList(translation), attribution);
    }

    public BackendTranslations(List<String> translations,
            @Nullable String attribution) {
        this.translations = translations;
        this.attribution = attribution;
    }

    @Nullable
    public List<String> getTranslations() {
        return translations;
    }

    public void setTranslations(List<String> translations) {
        this.translations = translations;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public int size() {
        return translations.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BackendTranslations)) return false;

        BackendTranslations that = (BackendTranslations) o;

        if (getTranslations() != null ?
            !getTranslations().equals(that.getTranslations()) :
            that.getTranslations() != null) return false;
        return getAttribution() != null ?
            getAttribution().equals(that.getAttribution()) :
            that.getAttribution() == null;

    }

    @Override
    public int hashCode() {
        int result =
            getTranslations() != null ? getTranslations().hashCode() : 0;
        result = 31 * result +
            (getAttribution() != null ? getAttribution().hashCode() : 0);
        return result;
    }
}
