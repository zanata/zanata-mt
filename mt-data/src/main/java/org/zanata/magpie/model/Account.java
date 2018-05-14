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
package org.zanata.magpie.model;

import java.util.Objects;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.collect.Sets;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
@NamedQueries(
        @NamedQuery(name = Account.QUERY_BY_USERNAME,
                query = "select u from Account u fetch all properties join u.credentials c where c.username = :username and u.enabled = true")
)
@Table(
        name = "account",
        uniqueConstraints = @UniqueConstraint(name = "UK_account_email", columnNames = {"email"})
)
public class Account extends ModelEntity {
    private static final long serialVersionUID = -8177299694942674381L;
    public static final String QUERY_BY_USERNAME = "QUERY_BY_USERNAME";

    @NotNull
    @Size(min = 1, max = 128)
    private String name;
    private String email;
    private AccountType accountType;

    @OneToMany(mappedBy = "account", cascade = { CascadeType.ALL },
            orphanRemoval = true)
    private Set<Credential> credentials = Sets.newHashSet();

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @JoinTable(name = "account_roles",
            joinColumns = { @JoinColumn(name = "account_id") })
    private Set<Role> roles = Sets.newHashSet();

    private boolean enabled = true;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY,
            mappedBy = "triggeredBy")
    private Set<TextFlowMTRequest> mtRequests;

    public Account(String name, String email,
            AccountType accountType, Set<Role> roles) {
        this.name = name;
        this.email = email;
        this.accountType = accountType;
        this.roles = roles;
    }

    public Account() {
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Set<Credential> getCredentials() {
        return credentials;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public void setCredentials(Set<Credential> credentials) {
        this.credentials = credentials;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<TextFlowMTRequest> getMtRequests() {
        return mtRequests;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return Objects.equals(name, account.name) &&
                Objects.equals(email, account.email) &&
                accountType == account.accountType &&
                Objects.equals(roles, account.roles);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, email, accountType, roles);
    }

    public boolean hasRole(String role) {
        return getRoles().stream().anyMatch(r -> role.equals(r.name()));
    }
}
