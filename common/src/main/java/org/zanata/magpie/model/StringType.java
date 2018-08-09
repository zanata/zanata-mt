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

import javax.ws.rs.core.MediaType;

import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.MediaType.TEXT_XML;

public enum StringType {
    TEXT_PLAIN,
    HTML,
    XML;

    public static StringType fromMediaType(MediaType mediaType) {
        if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) return TEXT_PLAIN;
        if (mediaType.equals(MediaType.TEXT_HTML_TYPE)) return HTML;
        if (mediaType.equals(MediaType.TEXT_XML_TYPE)) return XML;
        throw new IllegalArgumentException(mediaType.getType());
    }

    public static StringType fromMediaType(String mediaType) {
        switch (mediaType) {
            case MediaType.TEXT_PLAIN:
                return TEXT_PLAIN;
            case TEXT_HTML:
            case TEXT_XML:
                return TEXT_PLAIN;
            default:
                throw new IllegalArgumentException(mediaType);
        }
    }
}
