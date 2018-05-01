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

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.event.RequestedMTEvent;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;
import org.zanata.magpie.model.TextFlow;
import org.zanata.magpie.model.TextFlowMTRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Stateless
public class EventRecordingService implements Serializable {
    private static final Logger log =
            LoggerFactory.getLogger(EventRecordingService.class);
    private static final long serialVersionUID = 2744223109728759370L;

    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private EntityManager entityManager;

    public EventRecordingService() {
    }

    @Inject
    public EventRecordingService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Asynchronous
    @TransactionAttribute
    public void onMTRequest(@Observes(during = TransactionPhase.AFTER_COMPLETION) RequestedMTEvent event) {
        // need to re-fetch entities into hibernate session
        Document document =
                entityManager.find(Document.class, event.getDocument().getId());
        Locale locale =
                entityManager.find(Locale.class, event.getFromLocale().getId());
        TextFlowMTRequest textFlowMTRequest =
                new TextFlowMTRequest(event.getBackendID(), event.getEngineInvokeTime(),
                        document, locale, event.getTextFlows());

        log.info("invoked MT engine {} at {}", textFlowMTRequest.getBackendID(), textFlowMTRequest.getInvokeDate());

        entityManager.persist(textFlowMTRequest);
    }
}
