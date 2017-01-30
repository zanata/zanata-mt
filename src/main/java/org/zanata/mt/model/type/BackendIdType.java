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
import org.zanata.mt.model.BackendID;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BackendIdType extends AbstractSingleColumnStandardBasicType<BackendID>
    implements DiscriminatorType<BackendID> {

    private static final long serialVersionUID = 7345081786271438120L;

    public BackendIdType() {
        super(StringType.INSTANCE.getSqlTypeDescriptor(),
            BackendIdTypeDescriptor.INSTANCE);
    }

    @Override
    public String toString(BackendID value) {
        return String.valueOf((value).getId());
    }

    @Override
    public String getName() {
        return "entityType";
    }

    @Override
    public String objectToSQLString(BackendID value, Dialect dialect)
        throws Exception {
        return "\'" + toString(value) + "\'";
    }

    public BackendID stringToObject(String xml) throws Exception {
        if (xml.length() < 1) {
            throw new MappingException(
                "multiple or zero characters found parsing string");
        }
        return new BackendID(xml);
    }

    public BackendID fromStringValue(String xml) {
        return new BackendID(xml);
    }
}
