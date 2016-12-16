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

package org.zanata.mt.model.type;

import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.StringType;
import org.zanata.mt.model.Provider;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ProviderType extends AbstractSingleColumnStandardBasicType<Provider>
    implements DiscriminatorType<Provider> {

    private static final long serialVersionUID = 7345081786271438120L;

    public ProviderType() {
        super(StringType.INSTANCE.getSqlTypeDescriptor(),
            ProviderTypeDescriptor.INSTANCE);
    }

    @Override
    public String toString(Provider value) {
        return String.valueOf((value).name());
    }

    @Override
    public String getName() {
        return "entityType";
    }

    @Override
    public String objectToSQLString(Provider value, Dialect dialect)
        throws Exception {
        return "\'" + toString(value) + "\'";
    }

    public Provider stringToObject(String xml) throws Exception {
        if (xml.length() < 1) {
            throw new MappingException(
                "multiple or zero characters found parsing string");
        }
        return Provider.valueOf(xml);
    }

    public Provider fromStringValue(String xml) {
        return Provider.valueOf(xml);
    }
}
