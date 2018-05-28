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
package org.zanata.magpie.api.dto;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.zanata.magpie.model.BackendID;
import com.webcohesion.enunciate.metadata.DocumentationExample;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class MTRequestStatistics implements Serializable {
    private static final long serialVersionUID = 692848485827167853L;

    private String fromLocaleCode;
    private String toLocaleCode;
    private String docUrl;
    private long charCount;
    private long wordCount;
    private BackendID engine;

    public MTRequestStatistics() {
    }

    public MTRequestStatistics(String fromLocaleCode, String toLocaleCode,
            String docUrl, long charCount, long wordCount,
            BackendID engine) {
        this.fromLocaleCode = fromLocaleCode;
        this.toLocaleCode = toLocaleCode;
        this.docUrl = docUrl;
        this.charCount = charCount;
        this.wordCount = wordCount;
        this.engine = engine;
    }

    @NotNull
    @DocumentationExample("http://example.com")
    public String getDocUrl() {
        return docUrl;
    }

    @NotNull
    @Size(max = 128)
    @DocumentationExample("en-us")
    public String getFromLocaleCode() {
        return fromLocaleCode;
    }

    @NotNull
    @Size(max = 128)
    @DocumentationExample("fr")
    public String getToLocaleCode() {
        return toLocaleCode;
    }

    @DocumentationExample("50")
    public long getCharCount() {
        return charCount;
    }

    @DocumentationExample("10")
    public long getWordCount() {
        return wordCount;
    }

    @NotNull
    @DocumentationExample("Google")
    public BackendID getEngine() {
        return engine;
    }
}
