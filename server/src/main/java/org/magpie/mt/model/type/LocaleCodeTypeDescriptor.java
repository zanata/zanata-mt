/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.magpie.mt.model.type;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.magpie.mt.api.dto.LocaleCode;

public class LocaleCodeTypeDescriptor extends AbstractTypeDescriptor<LocaleCode> {
    private static final long serialVersionUID = 1L;
    public static final LocaleCodeTypeDescriptor INSTANCE =
            new LocaleCodeTypeDescriptor();

    protected LocaleCodeTypeDescriptor() {
        super(LocaleCode.class);
    }

    @Override
    public LocaleCode fromString(String string) {
        if (string == null) {
            return null;
        } else {
            return new LocaleCode(string);
        }
    }

    @Override
    public String toString(LocaleCode value) {
        return value.toString();
    }

    @Override
    public <X> X unwrap(LocaleCode value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (String.class.isAssignableFrom(type)) {
            return (X) value.toString();
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> LocaleCode wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (String.class.isInstance(value)) {
            return new LocaleCode((String) value);
        }
        throw unknownWrap(value.getClass());
    }
}
