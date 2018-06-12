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
package org.zanata.magpie.dao;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;

import org.zanata.magpie.model.Credential;
import org.zanata.magpie.model.Account;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class AccountDAO extends AbstractDAO<Account> {

    private static final long serialVersionUID = -5037944156984539025L;

    @SuppressWarnings("unused")
    public AccountDAO() {
    }

    @VisibleForTesting
    public AccountDAO(EntityManager entityManager) {
        setEntityManager(entityManager);
    }

    public Optional<Account> findAccountByUsername(String username) {
        List<Account> accounts = getEntityManager()
                .createNamedQuery(Account.QUERY_BY_USERNAME, Account.class)
                .setParameter("username", username)
                .getResultList();

        if (accounts.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(accounts.get(0));
        }
    }

    public Account saveCredentialAndAccount(Credential credential, Account account) {
        account.getCredentials().add(credential);
        getEntityManager().persist(account);
        getEntityManager().persist(credential);
        return account;
    }

    public List<Account> findAll() {
        return getEntityManager()
                .createNamedQuery(Account.QUERY_ALL_ACCOUNTS, Account.class)
                .getResultList();
    }

    public List<Account> findAllEnabled() {
        return getEntityManager()
                .createNamedQuery(Account.QUERY_ENABLED_ACCOUNTS, Account.class)
                .getResultList();
    }

    public Optional<Account> findAccountByEmail(@Nonnull String email) {
        List<Account> accounts = getEntityManager()
                .createNamedQuery(Account.QUERY_BY_EMAIL, Account.class)
                .setParameter("email", email)
                .getResultList();
        if (accounts.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(accounts.get(0));
    }
}
