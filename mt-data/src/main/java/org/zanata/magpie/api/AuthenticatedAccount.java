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
package org.zanata.magpie.api;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;

import org.zanata.magpie.model.Account;

/**
 * This is a mutable class that holds the authenticated account object.
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class AuthenticatedAccount implements Serializable {

    private static final long serialVersionUID = -7986651568502749384L;
    private @Nullable Account account;
    private String username;

    public void setAuthenticatedAccount(Account account) {
        this.account = account;
    }

    public boolean hasAuthenticatedAccount() {
        return account != null;
    }

    public Optional<Account> getAuthenticatedAccount() {
        return Optional.ofNullable(account);
    }

    public void setAuthenticatedUsername(String username) {
        this.username = username;
    }
    public Optional<String> getAuthenticatedUsername() {
        return Optional.ofNullable(username);
    }
}
