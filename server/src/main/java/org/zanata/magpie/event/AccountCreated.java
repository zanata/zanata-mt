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
package org.zanata.magpie.event;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.model.Role;

/**
 * This is just a CDI event.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 *
 * @see org.zanata.magpie.api.service.impl.AccountResourceImpl#registerNewAccount(AccountDto)
 */
public class AccountCreated {
    private String email;
    private Set<Role> roles;

    public AccountCreated(String email,
            Set<Role> roles) {
        this.email = email;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountCreated)) return false;
        AccountCreated that = (AccountCreated) o;
        return Objects.equals(getEmail(), that.getEmail()) &&
                Objects.equals(getRoles(), that.getRoles());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getEmail(), getRoles());
    }
}
