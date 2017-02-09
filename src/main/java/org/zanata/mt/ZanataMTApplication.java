package org.zanata.mt;

import java.util.Set;
import javax.ws.rs.ApplicationPath;

import org.zanata.mt.api.ArticleTranslatorResource;

import com.google.common.collect.ImmutableSet;
import org.zanata.mt.api.BackendResource;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationPath("api")
public class ZanataMTApplication extends javax.ws.rs.core.Application {
    private Set<Class<?>> classes = buildClasses();

    private static Set<Class<?>> buildClasses() {
        return ImmutableSet.<Class<?>>builder()
                // api classes
                .add(ArticleTranslatorResource.class)
                .add(BackendResource.class)
                // providers
                .build();
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
