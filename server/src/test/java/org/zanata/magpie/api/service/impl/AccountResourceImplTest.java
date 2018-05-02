package org.zanata.magpie.api.service.impl;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import javax.enterprise.event.Event;
import javax.ws.rs.core.Response;

import org.assertj.core.util.Sets;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.zanata.magpie.api.dto.APIResponse;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.api.dto.CredentialDto;
import org.zanata.magpie.exception.DataConstraintViolationException;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.AccountType;
import org.zanata.magpie.security.AccountCreated;
import org.zanata.magpie.service.AccountService;
import org.zanata.magpie.util.ValidatorProducer;


public class AccountResourceImplTest {

    private AccountResourceImpl accountResource;
    @Mock private AccountService accountService;
    @Mock private Event<AccountCreated> accountCreatedEvent;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        accountResource = new AccountResourceImpl(accountService,
                new ValidatorProducer().getValidator(), accountCreatedEvent);
        accountResource.uriInfo = new ResteasyUriInfo("account", "", "");
    }

    @Test
    public void badRequestIfAccountHasValidationError() {
        AccountDto accountDto = new AccountDto();
        assertThat(
                accountResource.registerNewAccount(new AccountDto()).getStatus())
                .isEqualTo(BAD_REQUEST.getStatusCode());
        accountDto.setName("name");
        accountDto.setAccountType(AccountType.Normal);
        accountDto.setCredentials(Sets.newHashSet());
        assertThat(
                accountResource.registerNewAccount(accountDto).getStatus())
                .isEqualTo(BAD_REQUEST.getStatusCode());

    }

    @Test
    public void badRequestIfCredentialIsEmpty() {
        AccountDto accountDto = new AccountDto();
        accountDto.setName("name");
        accountDto.setAccountType(AccountType.Normal);
        accountDto.setCredentials(Sets.newHashSet());

        Response response = accountResource.registerNewAccount(accountDto);
        assertThat(
                response.getStatus())
                .isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(((APIResponse) response.getEntity()).getTitle())
                .contains("credentials size must be between 1 and 10");

    }

    @Test
    public void badRequestIfCredentialIsInvalid() {
        AccountDto accountDto = new AccountDto();
        accountDto.setName("name");
        accountDto.setAccountType(AccountType.Normal);
        accountDto.setCredentials(Sets.newLinkedHashSet(new CredentialDto()));

        Response response = accountResource.registerNewAccount(accountDto);
        assertThat(
                response.getStatus())
                .isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(((APIResponse) response.getEntity()).getTitle())
                .contains("username may not be null");

    }

    @Test
    public void registerNewAccount() {
        AccountDto accountDto = new AccountDto();
        accountDto.setName("name");
        accountDto.setEmail("admin@example.com");
        accountDto.setAccountType(AccountType.Normal);
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setUsername("admin");
        char[] secret = "secret".toCharArray();
        credentialDto.setSecret(secret);
        accountDto.setCredentials(Sets.newLinkedHashSet(credentialDto));

        when(accountService.registerNewAccount(accountDto, "admin", secret)).thenAnswer(
                (Answer<AccountDto>) invocationOnMock -> {
                    accountDto.setId(1L);
                    return accountDto;
                });

        Response response = accountResource.registerNewAccount(accountDto);
        verify(accountCreatedEvent).fire(Mockito.any(AccountCreated.class));
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void registerDuplicateAccount() {
        AccountDto accountDto = new AccountDto();
        accountDto.setName("name");
        accountDto.setAccountType(AccountType.Normal);
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setUsername("admin");
        char[] secret = "secret".toCharArray();
        credentialDto.setSecret(secret);
        accountDto.setCredentials(Sets.newLinkedHashSet(credentialDto));
        when(accountService.registerNewAccount(accountDto, "admin", secret))
                .thenThrow(
                        new ConstraintViolationException("constraint violation",
                                new SQLException("key violation"),
                                "username must be unique"));

        assertThatThrownBy(() -> accountResource.registerNewAccount(accountDto))
                .isInstanceOf(DataConstraintViolationException.class);
    }

    @Test
    public void registerAccountFailedWithOtherException() {
        AccountDto accountDto = new AccountDto();
        accountDto.setName("name");
        accountDto.setAccountType(AccountType.Normal);
        CredentialDto credentialDto = new CredentialDto();
        credentialDto.setUsername("admin");
        char[] secret = "secret".toCharArray();
        credentialDto.setSecret(secret);
        accountDto.setCredentials(Sets.newLinkedHashSet(credentialDto));
        when(accountService.registerNewAccount(accountDto, "admin", secret))
                .thenThrow(
                        new IllegalStateException());

        assertThatThrownBy(() -> accountResource.registerNewAccount(accountDto))
                .isInstanceOf(MTException.class);
    }

}
