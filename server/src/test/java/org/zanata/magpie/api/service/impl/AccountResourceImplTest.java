package org.zanata.magpie.api.service.impl;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

import javax.enterprise.event.Event;
import javax.ws.rs.core.Response;

import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.api.dto.APIResponse;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.api.dto.CredentialDto;
import org.zanata.magpie.model.AccountType;
import org.zanata.magpie.security.AccountCreated;
import org.zanata.magpie.service.AccountService;
import org.zanata.magpie.util.ValidatorProducer;


public class AccountResourceImplTest {

    private AccountResourceImpl accountResource;
    @Mock private AccountService accountService;
    @Mock private Event<AccountCreated> unsetInitialPasswordEvent;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        accountResource = new AccountResourceImpl(accountService,
                new ValidatorProducer().getValidator(), unsetInitialPasswordEvent);
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


}
