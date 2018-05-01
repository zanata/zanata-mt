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
package org.zanata.magpie.event;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;

/**
 * CDI event that indicates some textflows has triggered a machine translation
 * engine call.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RequestedMTEvent implements Serializable {
    private static final long serialVersionUID = 2189882352044418886L;
    private final Document document;
    private final Locale fromLocale;
    private final List<String> textFlows;
    private final BackendID backendID;
    private final Date engineInvokeTime;

    public RequestedMTEvent(
            Document document, Locale fromLocale, List<String> textFlows,
            BackendID backendID, Date engineInvokeTime) {
        this.document = document;
        this.fromLocale = fromLocale;
        this.textFlows = textFlows;
        this.backendID = backendID;
        this.engineInvokeTime = new Date(engineInvokeTime.getTime());
    }

    public List<String> getTextFlows() {
        return textFlows;
    }

    public BackendID getBackendID() {
        return backendID;
    }

    public Date getEngineInvokeTime() {
        return new Date(engineInvokeTime.getTime());
    }

    public Document getDocument() {
        return document;
    }

    public Locale getFromLocale() {
        return fromLocale;
    }
}
