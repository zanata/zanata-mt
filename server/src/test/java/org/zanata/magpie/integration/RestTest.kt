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

import org.assertj.core.api.Assertions
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.junit.Assume
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType

object RestTest {
    private val log: Logger = LoggerFactory.getLogger(RestTest::class.java)
    val port = System.getProperty("http.port", "8080")
    val baseUrl = "http://localhost:$port/api/"
    val initialPassword by lazy { readInitialPasswordFromLog() }

    fun newClient(path: String) = ResteasyClientBuilder().build()
                .target(RestTest.baseUrl).path(path)

    fun setCommonHeaders(webTarget: WebTarget, username: String = "admin", token: String = "secret") = webTarget
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header("X-Auth-User", username)
                .header("X-Auth-Token", token)

    private fun readInitialPasswordFromLog(): String {
        val initialPasswordFile = File("/tmp/initialPassword")
        // we will write the initial password to file
        "docker cp MT:/opt/jboss/initialPassword $initialPasswordFile".runCommand(5)

        Assume.assumeTrue("can copy initialPassword file",
                initialPasswordFile.exists() && initialPasswordFile.canRead())

        val lines = Files.readAllLines(initialPasswordFile.toPath())
        Assume.assumeTrue("can read the file containing the initial password",
                lines != null && lines.size == 1)

        val initialPassword = lines[0]

        Assertions.assertThat(initialPassword).hasSize(32)
        return initialPassword
    }

    fun String.runCommand(timeout: Long, timeUnit: TimeUnit = TimeUnit.MINUTES) {
        var proc: Process? = null
        try {
            val parts = this.split("\\s".toRegex())
            proc = ProcessBuilder(*parts.toTypedArray())
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start()

            proc.waitFor(timeout, timeUnit)
        } catch(e: IOException) {
            log.error("error running command " + this, e)
            throw RuntimeException(e)
        } finally {
            proc?.destroy()
        }
    }
}
