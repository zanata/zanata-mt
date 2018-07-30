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
import org.junit.Assume
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zanata.magpie.api.dto.AccountDto
import org.zanata.magpie.model.AccountType
import org.zanata.magpie.model.Role
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Response

object EnsureAdminAccountRule : TestRule {
    private val log: Logger = LoggerFactory.getLogger(EnsureAdminAccountRule::class.java)

    val initialPassword by lazy { readInitialPasswordFromFile() }

    // this variable will be used by maven (so tests only create admin user once using initial password)
    private var adminUserCreated: Boolean = false

    /**
     * This is a more expensive way to test if admin user has been created on the server but it's more reliable.
     * In IDE we have to rely on this. In maven, the adminUserCreated variable will play its role.
     */
    private fun isAdminCreatedOnServer(): Boolean {
        val client = RestTest.setCommonHeaders(RestTest.newClient("account"), RestTest.adminUsername, RestTest.adminPassword)

        val response = client.get()
        return response.status == Response.Status.OK.statusCode
    }

    private fun createAdminAccountIfNeeded() {
        if (!adminUserCreated && !isAdminCreatedOnServer()) {
            // we can use initial password to authenticate as an admin and create a user
            val client = RestTest.setCommonHeaders(RestTest.newClient("account"), RestTest.adminUsername, initialPassword)
            val response = client.post(Entity.json(AccountDto(null, "Admin", "admin@example.com", RestTest.adminUsername, RestTest.adminPassword.toCharArray(),
                    AccountType.Normal, setOf(Role.admin))))

            Assertions.assertThat(response.status).isEqualTo(201)
            response.close()
            adminUserCreated = true
        }
    }

    private fun readInitialPasswordFromFile(): String {
        val initialPasswordFile = File("/tmp/initialPassword")
        // we will write the initial password to file
        "docker cp MT:/opt/jboss/magpie_initial_password $initialPasswordFile".runCommand(5)

        Assume.assumeTrue("can copy initialPassword file",
                initialPasswordFile.exists() && initialPasswordFile.canRead())

        val lines = Files.readAllLines(initialPasswordFile.toPath())
        Assume.assumeTrue("can read the file containing the initial password",
                lines != null && lines.size == 1)

        val initialPassword = lines[0]

        Assertions.assertThat(initialPassword).hasSize(32)
        return initialPassword
    }

    fun runCommand(cmdWithArgs: List<String>, timeout: Long,
                   timeUnit: TimeUnit = TimeUnit.MINUTES,
                   env: Map<String, String> = mapOf()) {
        var proc: Process? = null
        try {
            val processBuilder = ProcessBuilder(cmdWithArgs)
            processBuilder.environment().putAll(env)

            proc = processBuilder
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

    private fun String.runCommand(timeout: Long, timeUnit: TimeUnit = TimeUnit.MINUTES) {
        val parts = this.split("\\s".toRegex())
        runCommand(parts, timeout, timeUnit)
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                createAdminAccountIfNeeded()
                base.evaluate()
            }
        }
    }
}
