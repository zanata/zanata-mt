package org.zanata.mt.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ArticleType implements Serializable {

    public final static ArticleType KCS_ARTICLE = new ArticleType("KCS_ARTICLE");

    @NotNull
    private String type;

    public ArticleType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleType)) return false;

        ArticleType that = (ArticleType) o;

        return type != null ? type.equals(that.type) : that.type == null;

    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
