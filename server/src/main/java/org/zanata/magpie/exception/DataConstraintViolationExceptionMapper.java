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
package org.zanata.magpie.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.api.dto.APIResponse;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Provider
public class DataConstraintViolationExceptionMapper
        implements ExceptionMapper<DataConstraintViolationException> {
    private static final Logger LOG =
            LoggerFactory.getLogger(DataConstraintViolationExceptionMapper.class);

    @Override
    public Response toResponse(DataConstraintViolationException exception) {
        String title = "constraint violation exception";
        LOG.error(title, exception);
        Response.Status status = Response.Status.CONFLICT;
        APIResponse response = new APIResponse(status, exception, title);
        return Response.status(status).entity(response).build();
    }
}
