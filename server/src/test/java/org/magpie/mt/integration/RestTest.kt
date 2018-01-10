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
package org.magpie.mt.integration

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType

object RestTest {
    val port = System.getProperty("http.port", "8080")
    val baseUrl = "http://localhost:$port/api/"

    fun newClient(path: String) = ResteasyClientBuilder().build()
                .target(RestTest.baseUrl).path(path)

    fun setCommonHeaders(webTarget: WebTarget) = webTarget
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header("X-Auth-User", "devID")
                .header("X-Auth-Token", "devKEY")


}
