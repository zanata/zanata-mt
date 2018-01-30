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
package org.zanata.magpie.integration

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zanata.magpie.api.APIConstant
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType

object RestTest {
    private val log: Logger = LoggerFactory.getLogger(RestTest::class.java)
    val port = System.getProperty("http.port", "8080")
    val baseUrl = "http://localhost:$port/api/"
    val adminUsername = "admin"
    val adminSecret = "secret"

    fun newClient(path: String) = ResteasyClientBuilder().build()
                .target(RestTest.baseUrl).path(path)

    fun setCommonHeaders(webTarget: WebTarget, username: String, token: String) = webTarget
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(APIConstant.HEADER_USERNAME, username)
                .header(APIConstant.HEADER_API_KEY, token)

    fun setCommonHeadersAsAdmin(webTarget: WebTarget) = webTarget
            .request(MediaType.APPLICATION_JSON_TYPE)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .header(APIConstant.HEADER_USERNAME, adminUsername)
            .header(APIConstant.HEADER_API_KEY, adminSecret)

//    fun clearDatabaseTable(tableName: String) {
////        "docker exec MTDB psql --username=root --dbname=zanataMT --command=truncate account".runCommand(1)
//
//        val command = listOf("docker", "exec", "MTDB", "psql", "--username=root", "--dbname=zanataMT", "--command=TRUNCATE $tableName CASCADE")
//
//        runCommand(command, 1)
//    }
}
