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
package org.zanata.magpie.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;
import com.google.common.collect.Lists;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
public class TextFlowMTRequest implements Serializable {
    private static final long serialVersionUID = -6874010575058871413L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    @NotNull
    private Date invokeDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private BackendID backendID;

    @NaturalId
    @ManyToOne
    @JoinColumn(name = "document_id", updatable = false, nullable = false)
    @NotNull
    private Document document;

    @NaturalId
    @ManyToOne(optional = false)
    @JoinColumn(name = "locale_id", nullable = false, updatable = false)
    @NotNull
    private Locale locale;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_locale_id", nullable = false, updatable = false)
    @NotNull
    private Locale toLocale;

    @ManyToOne(optional = false)
    @JoinColumn(name = "triggered_account_id", nullable = false, updatable = false)
    @NotNull
    private Account triggeredBy;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = " textflow_contenthash")
    private List<String> textFlowContentHashes = Lists.newArrayList();

    public TextFlowMTRequest() {
    }

    public TextFlowMTRequest(BackendID backendID, Date invokeDate,
            Document document, Locale locale, Locale toLocale,
            Account triggeredBy,
            List<String> textFlowContentHashes) {
        this.backendID = backendID;
        this.invokeDate = new Date(invokeDate.getTime());
        this.document = document;
        this.locale = locale;
        this.toLocale = toLocale;
        this.triggeredBy = triggeredBy;
        this.textFlowContentHashes = textFlowContentHashes;
    }

    public Long getId() {
        return id;
    }

    public BackendID getBackendID() {
        return backendID;
    }

    public Date getInvokeDate() {
        return invokeDate == null ? null : new Date(invokeDate.getTime());
    }

    public Document getDocument() {
        return document;
    }

    public Locale getLocale() {
        return locale;
    }

    public Locale getToLocale() {
        return toLocale;
    }

    public List<String> getTextFlowContentHashes() {
        return textFlowContentHashes;
    }

    public Account getTriggeredBy() {
        return triggeredBy;
    }
}
