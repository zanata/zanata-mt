package org.zanata.mt.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ArticleTypeTest {

    @Test
    public void testConstructor() {
        ArticleType type = new ArticleType("type");
        assertThat(type.getType()).isEqualTo("type");
    }

    @Test
    public void testEqualsAndHashcode() {
        ArticleType type1 = new ArticleType("type");
        ArticleType type2 = new ArticleType("type");

        assertThat(type1.hashCode()).isEqualTo(type2.hashCode());
        assertThat(type1.equals(type2)).isTrue();

        type2 = new ArticleType("type2");
        assertThat(type1.hashCode()).isNotEqualTo(type2.hashCode());
        assertThat(type1.equals(type2)).isFalse();

        String test = "test";
        assertThat(type1.hashCode()).isNotEqualTo(test.hashCode());
        assertThat(type1.equals(test)).isFalse();
    }
}
