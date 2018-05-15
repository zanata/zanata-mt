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
package org.zanata.magpie.backend;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import org.zanata.magpie.api.dto.LocaleCode;

/**
 * A wrapper for locale code used in different MT engines.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class BackendLocaleCodeImpl implements BackendLocaleCode {
    private static final long serialVersionUID = -820475839897979047L;
    private String localeCode;

    public BackendLocaleCodeImpl(@NotNull LocaleCode localeCode) {
        this(localeCode.getId());
    }

    public BackendLocaleCodeImpl(@NotNull String localeCodeId) {
        this.localeCode = localeCodeId;
    }

    @Override
    public String getLocaleCode() {
        return localeCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BackendLocaleCodeImpl)) return false;
        BackendLocaleCodeImpl that = (BackendLocaleCodeImpl) o;
        return Objects.equals(getLocaleCode(), that.getLocaleCode());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getLocaleCode());
    }
}
