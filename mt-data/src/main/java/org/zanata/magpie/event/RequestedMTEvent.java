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

import javax.annotation.Nonnull;

import org.zanata.magpie.model.Account;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;
import com.google.common.base.MoreObjects;

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
    private final List<String> textFlows;
    private final BackendID backendID;
    private final Date engineInvokeTime;
    private final Account triggeredBy;
    private final long wordCount;
    private final long charCount;

    public RequestedMTEvent(
            @Nonnull Document document,
            List<String> textFlows,
            BackendID backendID, Date engineInvokeTime,
            Account account, long wordCount, long charCount) {
        this.document = document;
        this.textFlows = textFlows;
        this.backendID = backendID;
        this.engineInvokeTime = new Date(engineInvokeTime.getTime());
        triggeredBy = account;
        this.wordCount = wordCount;
        this.charCount = charCount;
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

    public Account getTriggeredBy() {
        return triggeredBy;
    }

    public long getWordCount() {
        return wordCount;
    }

    public long getCharCount() {
        return charCount;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("document", document.getUrl())
                .add("fromLocale", document.getFromLocale().getLocaleCode())
                .add("toLocale", document.getToLocale().getLocaleCode())
                .add("backendID", backendID)
                .add("engineInvokeTime", engineInvokeTime)
                .add("triggeredBy", triggeredBy)
                .add("wordCount", wordCount)
                .add("charCount", charCount)
                .toString();
    }
}
