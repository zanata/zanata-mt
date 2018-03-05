package org.zanata.magpie;

import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.google.common.collect.ImmutableSet;
import org.zanata.magpie.api.service.impl.BackendResourceImpl;
import org.zanata.magpie.api.service.impl.DocumentResourceImpl;
import org.zanata.magpie.api.service.impl.DocumentsResourceImpl;
import org.zanata.magpie.api.service.impl.InfoResourceImpl;
import org.zanata.magpie.api.service.impl.LanguagesResourceImpl;
import org.zanata.magpie.exception.BadRequestExceptionMapper;
import org.zanata.magpie.exception.MTExceptionMapper;

import static org.zanata.magpie.api.APIConstant.API_CONTEXT;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationPath(API_CONTEXT)
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
                .add(InfoResourceImpl.class)
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
