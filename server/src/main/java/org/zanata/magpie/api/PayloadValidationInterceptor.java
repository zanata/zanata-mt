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

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.api.dto.APIResponse;

import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Interceptor
@ValidatePayload
public class PayloadValidationInterceptor implements Serializable {
    private static final Logger log =
            LoggerFactory.getLogger(PayloadValidationInterceptor.class);

    private static final long serialVersionUID = -733258127968685778L;

    @SuppressFBWarnings("SE_BAD_FIELD")
    private Validator validator;

    @Inject
    public PayloadValidationInterceptor(Validator validator) {
        this.validator = validator;
    }

    public PayloadValidationInterceptor() {
    }

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocation) throws Exception {
        ValidatePayload validatePayload =
                getAnnotationFromClassOrMethod(invocation);
        Class payloadType = validatePayload.value();
        Optional<Object> matchedParam =
                Lists.newArrayList(invocation.getParameters()).stream().filter(
                        payloadType::isInstance).findAny();

        if (matchedParam.isPresent()) {
            Object param = matchedParam.get();
            Set<ConstraintViolation<Object>> violations =
                    validator.validate(param);

            if (!violations.isEmpty()) {
                String message =
                        violations.stream().map(
                                v -> String.format("%s %s", v.getPropertyPath(), v.getMessage()))
                                .reduce("error: ", (a, b) -> a + b + "; ");
                log.info("invalid payload for {}: {}", invocation.getMethod(), message);

                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new APIResponse(Response.Status.BAD_REQUEST, message))
                        .build();
            }
        }

        return invocation.proceed();
    }

    private ValidatePayload getAnnotationFromClassOrMethod(
            InvocationContext invocation) {
        ValidatePayload validatePayload = invocation.getTarget().getClass()
                .getAnnotation(ValidatePayload.class);
        if (validatePayload == null) {
            validatePayload = invocation.getMethod().getAnnotation(ValidatePayload.class);
        }
        return validatePayload;
    }
}
