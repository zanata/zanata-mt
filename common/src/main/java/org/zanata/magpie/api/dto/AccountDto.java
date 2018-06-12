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

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.magpie.model.AccountType;
import org.zanata.magpie.model.Role;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@JsonSerialize
public class AccountDto {
    private Long id;
    private String name;
    private String email;
    private AccountType accountType;
    private Set<Role> roles;
    private Set<CredentialDto> credentials;

    public AccountDto(Long id, String name, String email,
            AccountType accountType, Set<Role> roles,
            Set<CredentialDto> credentials) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.accountType = accountType;
        this.roles = roles;
        this.credentials = credentials;
    }

    public AccountDto(Long id, String name, String email,
            AccountType accountType, Set<Role> roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.accountType = accountType;
        this.roles = roles;
    }

    public AccountDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotNull
    @Size(min = 1, max = 128)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NotNull
    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Valid
    @NotNull
    @Size(min = 1, max = 10)
    public Set<CredentialDto> getCredentials() {
        return credentials;
    }

    public void setCredentials(
            Set<CredentialDto> credentials) {
        this.credentials = credentials;
    }
}
