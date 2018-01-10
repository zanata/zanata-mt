package org.magpie.mt;

import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.google.common.collect.ImmutableSet;
import org.magpie.mt.api.service.impl.BackendResourceImpl;
import org.magpie.mt.api.service.impl.DocumentResourceImpl;
import org.magpie.mt.api.service.impl.DocumentsResourceImpl;
import org.magpie.mt.api.service.impl.LanguagesResourceImpl;
import org.magpie.mt.exception.BadRequestExceptionMapper;
import org.magpie.mt.exception.MTExceptionMapper;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationPath("api")
public class MTApplication extends Application {
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
                .add(BadRequestExceptionMapper.class)
                .add(MTExceptionMapper.class)
                .build();
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
