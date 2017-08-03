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
package org.zanata.mt.annotation;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class EnvVariableProducer {
    private static final Logger log =
            LoggerFactory.getLogger(EnvVariableProducer.class);
    @Produces
    @EnvVariable("")
    String findProperty(InjectionPoint ip) {
        EnvVariable annotation = ip.getAnnotated()
                .getAnnotation(EnvVariable.class);

        String name = annotation.value();
        String found = getEnv(name);
        if (found == null) {
            log.warn(
                    "Environment variable '{}' is not defined!", name);
        }
        return Strings.nullToEmpty(found);
    }

    String getEnv(String name) {
        return System.getenv(name);
    }
}
