package org.zanata.mt;

import java.util.Set;
import javax.ws.rs.ApplicationPath;

import com.google.common.collect.ImmutableSet;
import org.zanata.mt.api.service.impl.BackendResourceImpl;
import org.zanata.mt.api.service.impl.DocumentResourceImpl;
import org.zanata.mt.api.service.impl.DocumentsResourceImpl;
import org.zanata.mt.api.service.impl.LanguagesResourceImpl;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationPath("api")
public class ZanataMTApplication extends javax.ws.rs.core.Application {
    private Set<Class<?>> classes = buildClasses();

    private static Set<Class<?>> buildClasses() {
        return ImmutableSet.<Class<?>>builder()
                // api classes
                .add(DocumentResourceImpl.class)
                .add(BackendResourceImpl.class)
                .add(LanguagesResourceImpl.class)
                .add(DocumentsResourceImpl.class)
                // providers
                .build();
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
