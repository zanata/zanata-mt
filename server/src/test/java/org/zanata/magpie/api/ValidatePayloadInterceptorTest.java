/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import javax.interceptor.InvocationContext;
import javax.ws.rs.core.Response;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.api.dto.CredentialDto;
import org.zanata.magpie.api.service.impl.AccountResourceImpl;
import org.zanata.magpie.model.AccountType;
import org.zanata.magpie.producer.ValidatorProducer;

import com.google.common.collect.Sets;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ValidatePayloadInterceptorTest {
    @Mock
    private InvocationContext invocationContext;

    private ValidatePayloadInterceptor interceptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        interceptor = new ValidatePayloadInterceptor(new ValidatorProducer().getValidator());
        when(invocationContext.getTarget())
                .thenReturn(new AccountResourceImpl());
        List<Method> methods = Lists.newArrayList(
                AccountResourceImpl.class.getDeclaredMethods());
        Optional<Method> methodWithAnno = methods.stream()
                .filter(m -> m.getAnnotation(ValidatePayload.class) != null)
                .findAny();
        when(invocationContext.getMethod()).thenReturn(methodWithAnno.get());
    }


    @Test
    public void badRequestIfAccountHasValidationError() throws Exception {
        AccountDto accountDto = new AccountDto();
        when(invocationContext.getParameters()).thenReturn(new Object[] {accountDto});

        Object result = interceptor.aroundInvoke(invocationContext);
        assertThat(result).isInstanceOf(Response.class);
        assertThat(((Response) result).getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

        accountDto.setName("name");
        accountDto.setAccountType(AccountType.Normal);
        accountDto.setCredentials(Sets.newHashSet());
        result = interceptor.aroundInvoke(invocationContext);
        assertThat(result).isInstanceOf(Response.class);
        assertThat(((Response) result).getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void proceedIfThereIsNoValidationError() throws Exception {
        CredentialDto credentialDto =
                new CredentialDto("username", "secret".toCharArray());
        AccountDto accountDto = new AccountDto(1L, "name", "name@example.com",
                AccountType.ServiceAccount, Sets.newHashSet(),
                Sets.newHashSet(credentialDto));
        when(invocationContext.getParameters())
                .thenReturn(new Object[]{ accountDto });
        Object expectedResult = new Object();
        when(invocationContext.proceed()).thenReturn(expectedResult);

        Object result = interceptor.aroundInvoke(invocationContext);
        assertThat(result).isSameAs(expectedResult);

    }

}
