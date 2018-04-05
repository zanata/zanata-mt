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
package org.zanata.magpie.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.clustering.dispatcher.Command;
import org.wildfly.clustering.group.Node;
import org.zanata.magpie.util.PasswordUtil;

import com.google.common.collect.Lists;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class InitialPasswordCommand implements Command<String, Node> {
    private static final Logger log =
            LoggerFactory.getLogger(InitialPasswordCommand.class);
    static final Path INITIAL_PASSWORD_FILE = Paths.get(System.getProperty("user.home"),
            "magpie_initial_password");
    private final String initialPassword;

    public InitialPasswordCommand(String initialPassword) {
        this.initialPassword = initialPassword;
    }


    @Override
    public String execute(Node node) throws Exception {
//        log.info("=== no account exists in the system ===");
//        log.info("=== to authenticate, use admin as username and ===");
//        log.info("=== initial password (without leading spaces):  {}", initialPassword);
//        log.info("=== initial password is also written to:  {}",
//                INITIAL_PASSWORD_FILE);
//        log.info("=======================================");
        try {
            Files.write(INITIAL_PASSWORD_FILE,
                    Lists.newArrayList(this.initialPassword));
        } catch (IOException e) {
            log.warn("failed writing initial password to disk", e);
        }
        try {
            Runtime.getRuntime()
                    .exec(new String[] {"chmod", "400", INITIAL_PASSWORD_FILE
                            .toAbsolutePath().toString()});
        } catch (IOException e) {
            log.info("unable to change permission on {}",
                    INITIAL_PASSWORD_FILE);
        }
        return initialPassword;
    }
}
