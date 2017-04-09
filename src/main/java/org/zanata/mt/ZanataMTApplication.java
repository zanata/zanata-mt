package org.zanata.mt;

import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.google.common.collect.ImmutableSet;
import org.zanata.mt.api.service.impl.BackendResourceImpl;
import org.zanata.mt.api.service.impl.DocumentResourceImpl;
import org.zanata.mt.api.service.impl.DocumentsResourceImpl;
import org.zanata.mt.api.service.impl.LanguagesResourceImpl;
import org.zanata.mt.exception.BadRequestExceptionMapper;
import org.zanata.mt.exception.InternalExceptionMapper;
import org.zanata.mt.exception.ZanataMTExceptionMapper;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationPath("api")
public class ZanataMTApplication extends Application {
    private Set<Class<?>> classes = buildResources();

    private static Set<Class<?>> buildResources() {
        return ImmutableSet.<Class<?>>builder()
                .addAll(buildAPIResource())
                .addAll(buildExceptionMapperResource())
                .build();
    }

    // build api resources
    private static Set<Class<?>> buildAPIResource() {
        return ImmutableSet.<Class<?>>builder()
                .add(DocumentResourceImpl.class)
                .add(BackendResourceImpl.class)
                .add(LanguagesResourceImpl.class)
                .add(DocumentsResourceImpl.class)
                .build();
    }

    // build exception mapper class
    private static Set<Class<?>> buildExceptionMapperResource() {
        return ImmutableSet.<Class<?>>builder()
                .add(InternalExceptionMapper.class)
                .add(BadRequestExceptionMapper.class)
                .add(ZanataMTExceptionMapper.class)
                .build();
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
