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
package org.zanata.magpie.api.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.magpie.api.APIConstant;
import org.zanata.magpie.api.dto.MTRequestStatistics;
import org.zanata.magpie.dto.DateRange;

import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.RequestHeaders;
import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * API for reporting.
 */
@Path("/report")
@RequestHeaders({
        @RequestHeader(name = APIConstant.HEADER_USERNAME,
                description = "The authentication user."),
        @RequestHeader(name = APIConstant.HEADER_API_KEY,
                description = "The authentication token.") })
@ResourceLabel("Reporting")
public interface ReportingResource {

    /**
     * Get the statistics for machine translation back end during a given date
     * range, for requests by the current user.
     *
     * @param dateRange
     *         date range for the reports. Format: from..to (yyyy-mm-dd..yyyy-mm-dd)
     * @return statistics about character count and word count
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = 200, condition = "List of requests that sent to the MT backend",
                    type = @TypeHint(MTRequestStatistics[].class)),
            @ResponseCode(code = 500, condition = "Unexpected error") })
    @TypeHint(MTRequestStatistics[].class)
    Response getMachineTranslationUsage(
            @QueryParam("dateRange") DateRange dateRange);
}
