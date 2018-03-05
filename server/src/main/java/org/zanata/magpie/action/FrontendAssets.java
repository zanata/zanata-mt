/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.magpie.action;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Named;
import javax.servlet.ServletContext;
import java.io.InputStream;
import java.io.Serializable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 **/
@ApplicationScoped
@Named("frontendAssets")
public class FrontendAssets implements Serializable {
    private static final Logger LOG =
        LoggerFactory.getLogger(FrontendAssets.class);

    private final static String MANIFEST_PATH = "META-INF/resources/manifest.json";

    private FrontendManifest manifest;
    private String frontendJs;
    private String frontendCss;
    private String vendorJs;

    public void onInit(
        @Observes @Initialized(ApplicationScoped.class) ServletContext servletContext)
        throws Exception {
        manifest = getManifest();
        String contextPath = servletContext.getContextPath();
        frontendJs = generateAbsolutePath(contextPath, manifest.getFrontendJs());
        frontendCss = generateAbsolutePath(contextPath, manifest.getFrontendCss());
        vendorJs = generateAbsolutePath(contextPath, manifest.getVendorJs());

        LOG.info("Frontend manifest: {}", manifest);
    }

    protected FrontendManifest getManifest() throws Exception {
        InputStream manifestResource =
            Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(MANIFEST_PATH);
        if (manifestResource == null) {
            throw new IllegalStateException(
                "can not load manifest.json from " + MANIFEST_PATH +
                    ". Did you forget to build and include frontend?");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(manifestResource, FrontendManifest.class);
    }

    public String getFrontendJs() {
        return frontendJs;
    }

    public String getFrontendCss() {
        return frontendCss;
    }

    public String getVendorJs() {
        return vendorJs;
    }

    public String generateAbsolutePath(String contextPath, String file) {
        return contextPath + "/" + StringUtils.stripStart(file, "/");
    }
}
