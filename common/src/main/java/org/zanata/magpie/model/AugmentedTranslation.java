package org.zanata.magpie.model;

import javax.annotation.Nullable;

/**
 * Object for extracted plainTranslation and raw plainTranslation
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class AugmentedTranslation {
    private String plainTranslation;
    private String rawTranslation;

    public AugmentedTranslation(String plainTranslation, String rawTranslation) {
        this.plainTranslation = plainTranslation;
        this.rawTranslation = rawTranslation;
    }

    public String getPlainTranslation() {
        return plainTranslation;
    }

    public void setPlainTranslation(String plainTranslation) {
        this.plainTranslation = plainTranslation;
    }

    public String getRawTranslation() {
        return rawTranslation;
    }

    public void setRawTranslation(String rawTranslation) {
        this.rawTranslation = rawTranslation;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof AugmentedTranslation)) return false;

        AugmentedTranslation augmentedTranslation = (AugmentedTranslation) o;

        if (plainTranslation != null ? !plainTranslation
            .equals(augmentedTranslation.plainTranslation) :
            augmentedTranslation.plainTranslation != null) return false;
        return rawTranslation != null ? rawTranslation
            .equals(augmentedTranslation.rawTranslation) :
            augmentedTranslation.rawTranslation == null;

    }

    @Override
    public int hashCode() {
        int result = plainTranslation != null ? plainTranslation.hashCode() : 0;
        result = 31 * result + (rawTranslation != null ? rawTranslation.hashCode() : 0);
        return result;
    }
}
