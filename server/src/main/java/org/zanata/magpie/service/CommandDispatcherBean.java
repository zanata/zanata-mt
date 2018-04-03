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

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.wildfly.clustering.dispatcher.Command;
import org.wildfly.clustering.dispatcher.CommandDispatcher;
import org.wildfly.clustering.dispatcher.CommandDispatcherFactory;
import org.wildfly.clustering.dispatcher.CommandResponse;
import org.wildfly.clustering.group.Node;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
@Startup
public class CommandDispatcherBean {
    @EJB
    private CommandDispatcherFactory factory;
    private CommandDispatcher<Node> dispatcher;

    @PostConstruct
    public void init() {
        dispatcher = factory.createCommandDispatcher(
                "CommandDispatcher", factory.getGroup().getLocalNode());
    }

    @PreDestroy
    public void destroy() {
        close();
    }

    public <R> CommandResponse<R> executeOnNode(Command<R, Node> command,
            Node node) throws Exception {
        return dispatcher.executeOnNode(command, node);
    }

    public <R> Map<Node, CommandResponse<R>> executeOnCluster(
            Command<R, Node> command, Node... excludedNodes) throws Exception {
        return dispatcher.executeOnCluster(command, excludedNodes);
    }

    public void close() {
        dispatcher.close();
    }
}
