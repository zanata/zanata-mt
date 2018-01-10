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

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.LiteralType;
import org.hibernate.type.StringType;
import org.magpie.mt.api.dto.LocaleCode;

public class LocaleCodeType extends
    AbstractSingleColumnStandardBasicType<LocaleCode> implements
    LiteralType<LocaleCode> {

    private static final long serialVersionUID = 3064881963824869913L;

    public LocaleCodeType() {
        super(StringType.INSTANCE.getSqlTypeDescriptor(),
                LocaleCodeTypeDescriptor.INSTANCE);
    }

    @Override
    public String getName() {
        return "localeCode";
    }

    @Override
    public String objectToSQLString(LocaleCode value, Dialect dialect)
            throws Exception {
        return "\'" + toString(value) + "\'";
    }

}
