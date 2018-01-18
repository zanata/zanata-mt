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
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;

import org.zanata.magpie.model.AccountType;
import org.zanata.magpie.model.LocalCredential;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.model.Role;
import org.zanata.magpie.util.PasswordUtil;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class AccountDAO extends AbstractDAO<Account> {

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

    public Account saveLocalAccount(String name, String email, AccountType accountType, Set<Role> roles,
            String username, char[] secret) {
        PasswordUtil passwordUtil = new PasswordUtil();
        Account account = new Account(name, email, accountType, roles);
        LocalCredential credential = new LocalCredential(account, username,
                passwordUtil.hash(secret));
        account.getCredentials().add(credential);
        getEntityManager().persist(account);
        getEntityManager().persist(credential);
        return account;
    }

    public List<Account> findAll() {
        return getEntityManager()
                .createQuery("from Account a join fetch a.roles order by a.creationDate", Account.class)
                .getResultList();
    }

    public List<Account> findAllEnabled() {
        return getEntityManager()
                .createQuery("from Account a join fetch a.roles where enabled = true order by creationDate", Account.class)
                .getResultList();
    }
}
