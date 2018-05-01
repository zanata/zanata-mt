/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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

package org.zanata.magpie.backend.deepL.internal.dto;

import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCode;

import javax.validation.constraints.NotNull;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DeepLLocaleCode implements BackendLocaleCode {
    private String localeCode;

    public DeepLLocaleCode(@NotNull LocaleCode localeCode) {
        this(localeCode.getId());
    }

    public DeepLLocaleCode(@NotNull String localeCode) {
        this.localeCode = localeCode;
    }

    @Override
    public String getLocaleCode() {
        return localeCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeepLLocaleCode)) return false;

        DeepLLocaleCode that = (DeepLLocaleCode) o;

        return getLocaleCode() != null ?
                getLocaleCode().equals(that.getLocaleCode()) :
                that.getLocaleCode() == null;
    }

    @Override
    public int hashCode() {
        return getLocaleCode() != null ? getLocaleCode().hashCode() : 0;
    }
}
