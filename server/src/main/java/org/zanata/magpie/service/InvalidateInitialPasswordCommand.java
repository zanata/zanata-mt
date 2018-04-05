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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.clustering.dispatcher.Command;
import org.wildfly.clustering.group.Node;

import static org.zanata.magpie.service.InitialPasswordCommand.INITIAL_PASSWORD_FILE;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class InvalidateInitialPasswordCommand implements Command<String, Node> {
    private static final Logger log =
            LoggerFactory.getLogger(InvalidateInitialPasswordCommand.class);
    @Override
    public String execute(Node node) throws Exception {
        try {
            Files.delete(INITIAL_PASSWORD_FILE);
        } catch (IOException e) {
            log.warn("unable to delete {}. {}", INITIAL_PASSWORD_FILE,
                    e.getMessage());
        }
        return node.getName();
    }
}