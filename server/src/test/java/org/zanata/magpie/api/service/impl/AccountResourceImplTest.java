package org.zanata.magpie.api.service.impl;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.zanata.magpie.event.AccountCreated;
import org.zanata.magpie.producer.ValidatorProducer;
import org.zanata.magpie.service.AccountService;


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

    @Test
    public void updateAccount() {
        AccountDto accountDto = new AccountDto();
        when(accountService.updateAccount(accountDto)).thenReturn(true);

        Response response = accountResource.updateAccount(accountDto);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void returnNotFoundIfUpdateAccountFailed() {
        AccountDto accountDto = new AccountDto();
        when(accountService.updateAccount(accountDto)).thenReturn(false);

        Response response = accountResource.updateAccount(accountDto);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testLoginInvalid() {
        Response response = accountResource.login("");
        assertThat(response.getStatus()).isEqualTo(401);

        accountResource.login("testing 124");
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void testFailLogin() {
        accountResource.uriInfo = new ResteasyUriInfo("account/login", "", "");

        String header = "hmac test:U2FsdGVkX18yRsxRcAMD+FUvQ1OqXIoSpps96iVs/Ug=";
        when(accountService.authenticate("test", "testing")).thenReturn(Optional
            .empty());
        Response response = accountResource.login(header);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void testLogin() {
        accountResource.uriInfo = new ResteasyUriInfo("account/login", "", "");
        AccountDto accountDto = new AccountDto();
        String header = "hmac test:U2FsdGVkX18yRsxRcAMD+FUvQ1OqXIoSpps96iVs/Ug=";
        when(accountService.authenticate("test", "testing")).thenAnswer(
            (Answer<Optional<AccountDto>>) invocationOnMock -> {
                accountDto.setId(1L);
                return Optional.of(accountDto);
            });
        Response response = accountResource.login(header);
        assertThat(response.getStatus()).isEqualTo(200);
    }

}
