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
package org.zanata.mt.api.dto;

import org.zanata.mt.model.BackendID;
import com.google.common.base.Strings;

public enum TranslationProvider {
    Microsoft(BackendID.MS), Google(BackendID.GOOGLE), Dev(BackendID.DEV);

    private final BackendID backendID;

    TranslationProvider(BackendID backendID) {
        this.backendID = backendID;
    }

    /**
     * JAX-RS will use this method to marshall between string and this enum.
     * Supports abbreviation. e.g. 'm' will map to 'Microsoft'.
     *
     * @param value
     *            string representation of the enum constants
     * @return the enum constant
     */
    public static TranslationProvider fromString(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        if (value.toLowerCase().startsWith("m")) {
            return Microsoft;
        }
        if (value.toLowerCase().startsWith("g")) {
            return Google;
        }
        if (value.equalsIgnoreCase(Dev.name())) {
            return Dev;
        }
        throw new IllegalArgumentException(
                "can not parse [" + value + "] to a translation provider");
    }

    public BackendID getBackendID() {
        return backendID;
    }
}
