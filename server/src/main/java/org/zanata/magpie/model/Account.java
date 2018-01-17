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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Sets;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
@NamedQueries(
        @NamedQuery(name = Account.QUERY_BY_USERNAME,
                query = "select u from Account u join u.credentials c where c.username = :username")
)
@Table(
        name = "account",
        uniqueConstraints = @UniqueConstraint(name = "AccountEmailAccountTypeUnqiue", columnNames = {"email", "accountType"})
)
public class Account extends ModelEntity {
    public static final String QUERY_BY_USERNAME = "QUERY_BY_USERNAME";

    @NotNull
    private String name;
    private String email;
    private AccountType accountType;

    @OneToMany(mappedBy = "account", cascade = { CascadeType.ALL },
            orphanRemoval = true)
    private Set<Credential> credentials = Sets.newHashSet();

    @ElementCollection
    @JoinTable(name = "account_roles",
            joinColumns = { @JoinColumn(name = "account_id") })
    private Set<String> roles = Sets.newHashSet();

    private boolean enabled = true;

    public Account(String name, String email,
            AccountType accountType, Set<String> roles) {
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

    public Set<String> getRoles() {
        return roles;
    }

    public Set<Credential> getCredentials() {
        return credentials;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

}
