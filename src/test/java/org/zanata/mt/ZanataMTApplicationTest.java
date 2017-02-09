package org.zanata.mt;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.zanata.mt.api.ArticleTranslatorResource;
import org.zanata.mt.api.BackendResource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ZanataMTApplicationTest {

    private ZanataMTApplication zanataMTApplication;

    @Before
    public void setup() {
        zanataMTApplication = new ZanataMTApplication();
    }

    @Test
    public void testGetClasses() {
        Set<Class> expectedClasses = ImmutableSet.of(
                ArticleTranslatorResource.class, BackendResource.class);

        Set<Class<?>> classes = zanataMTApplication.getClasses();

        assertThat(classes).isEqualTo(expectedClasses);
    }
}
