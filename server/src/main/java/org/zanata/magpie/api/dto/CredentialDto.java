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
package org.zanata.magpie.api.dto;

import java.util.Arrays;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CredentialDto {
    private String username;
    private char[] secret;

    public CredentialDto(String username, char[] secret) {
        this.username = username;
        this.secret = nullSafeArrayCopy(secret);
    }

    public CredentialDto() {
    }

    @NotNull
    @Size(min = 2, max = 20)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @NotNull
    @Size(min = 6)
    public char[] getSecret() {
        return nullSafeArrayCopy(secret);
    }

    public void setSecret(char[] secret) {
        this.secret = nullSafeArrayCopy(secret);
    }

    private static char[] nullSafeArrayCopy(char[] arr) {
        if (arr == null) {
            return null;
        }
        return Arrays.copyOf(arr, arr.length);
    }
}
