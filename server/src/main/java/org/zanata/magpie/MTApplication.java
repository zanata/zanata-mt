/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.magpie;

import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.google.common.collect.ImmutableSet;
import org.zanata.magpie.api.security.SecurityInterceptor;
import org.zanata.magpie.api.service.impl.AccountResourceImpl;
import org.zanata.magpie.api.service.impl.BackendResourceImpl;
import org.zanata.magpie.api.service.impl.DocumentResourceImpl;
import org.zanata.magpie.api.service.impl.DocumentsResourceImpl;
import org.zanata.magpie.api.service.impl.InfoResourceImpl;
import org.zanata.magpie.api.service.impl.LanguagesResourceImpl;
import org.zanata.magpie.api.service.impl.ReportingResourceImpl;
import org.zanata.magpie.exception.AccessDeniedExceptionMapper;
import org.zanata.magpie.exception.BadRequestExceptionMapper;
import org.zanata.magpie.exception.DataConstraintViolationExceptionMapper;
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
                .addAll(buildOtherProviders())
                .build();
    }

    // build api resources
    private static Set<Class<?>> buildAPIResource() {
        return ImmutableSet.<Class<?>>builder()
                .add(DocumentResourceImpl.class)
                .add(BackendResourceImpl.class)
                .add(LanguagesResourceImpl.class)
                .add(DocumentsResourceImpl.class)
                .add(AccountResourceImpl.class)
                .add(InfoResourceImpl.class)
                .add(ReportingResourceImpl.class)
                .build();
    }

    // build exception mapper class
    private static Set<Class<?>> buildExceptionMapperResource() {
        return ImmutableSet.<Class<?>>builder()
                .add(BadRequestExceptionMapper.class)
                .add(AccessDeniedExceptionMapper.class)
                .add(MTExceptionMapper.class)
                .add(DataConstraintViolationExceptionMapper.class)
                .build();
    }

    // other providers, e.g. interceptors
    private static Set<Class<?>> buildOtherProviders() {
        return ImmutableSet.<Class<?>>builder()
                .add(SecurityInterceptor.class)
                .build();
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
