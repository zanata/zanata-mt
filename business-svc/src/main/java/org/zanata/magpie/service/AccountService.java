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
package org.zanata.magpie.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.dao.AccountDAO;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.util.PasswordUtil;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class AccountService implements Serializable {
    private static final long serialVersionUID = -7045985475911143937L;
    private static final Logger log =
            LoggerFactory.getLogger(AccountService.class);

    private AccountDAO accountDAO;
    private final PasswordUtil passwordUtil = new PasswordUtil();

    @Inject
    public AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @SuppressWarnings("unused")
    public AccountService() {
    }

    @Transactional
    public AccountDto registerNewAccount(AccountDto accountDto, String username,
            char[] password) {
        String passwordHash = passwordUtil.hash(password);

        Account account =
            new Account(accountDto.getName(), accountDto.getEmail(), username,
                passwordHash, accountDto.getAccountType(),
                accountDto.getRoles());

        account = accountDAO.persist(account);

        accountDto.setId(account.getId());
        return accountDto;
    }

    /**
     * Try to authenticate using given username and password.
     *
     * @return the matching Account if there is a match on the username and
     *         password.
     */
    public Optional<Account> authenticate(String username, String password) {
        Optional<Account> account = accountDAO.findAccountByUsername(username);
        return account.flatMap(acc -> {
            if (passwordUtil.authenticate(password.toCharArray(),
                acc.getPasswordHash())) {
                return account;
            } else {
                return Optional.empty();
            }
        });
    }

    public List<AccountDto> getAllAccounts(boolean showDisabled) {
        List<Account> result;
        if (showDisabled) {
            result = accountDAO.findAll();
        } else {
            result = accountDAO.findAllEnabled();
        }
        return result.stream()
                .map(a -> new AccountDto(a.getId(), a.getName(), a.getEmail(),
                        a.getAccountType(), a.getRoles()))
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean updateAccount(AccountDto accountDto) {
        Optional<Account> account =
                accountDAO.findAccountByEmail(accountDto.getEmail());
        account.ifPresent(a -> {
            log.info("updating account {}", a.getEmail());
            a.setAccountType(accountDto.getAccountType());
            a.setName(accountDto.getName());
            a.setRoles(accountDto.getRoles());
            a.setUsername(accountDto.getUsername());
            a.setPasswordHash(passwordUtil.hash(accountDto.getPassword()));
        });
        return account.isPresent();
    }
}
