/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.magpie.api.service.impl;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.zanata.magpie.annotation.BackEndProviders;
import org.zanata.magpie.api.InputStreamStreamingOutput;
import org.zanata.magpie.api.dto.APIResponse;
import org.zanata.magpie.api.service.BackendResource;
import org.zanata.magpie.model.BackendID;
import com.google.common.annotations.VisibleForTesting;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class BackendResourceImpl implements BackendResource {

    private Set<BackendID> availableProviders;

    @SuppressWarnings("unused")
    public BackendResourceImpl() {
    }

    @Inject
    public BackendResourceImpl(@BackEndProviders Set<BackendID> availableProviders) {
        this.availableProviders = availableProviders;
    }

    @Override
    public Response getAttribution(@QueryParam("id") String id) {
        Optional<APIResponse> response = validateId(id);
        if (response.isPresent()) {
            return Response.status(response.get().getStatus())
                    .entity(response.get())
                    .type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        BackendID backendID = BackendID.fromString(id);
        String imageResource = getAttributionImageResource(backendID);
        String docName = new File(imageResource).getName();

        InputStream is = getResourceAsStream(imageResource);
        if (is == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponse(Response.Status.NOT_FOUND,
                            "attribution image can not be found: " + imageResource))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .build();
        }
        StreamingOutput output =
                new InputStreamStreamingOutput(is);
        return Response.ok().header("Content-Disposition",
                "attachment; filename=\"" + docName + "\"")
                .entity(output).type("image/png").build();
    }

    @Override
    public Response getStringAttribution(String id) {
        Optional<APIResponse> response = validateId(id);
        if (response.isPresent()) {
            return Response.status(response.get().getStatus())
                .entity(response.get())
                .type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        BackendID backendID = BackendID.fromString(id);
        String attr;
        switch (backendID) {
            case MS:
                attr = MS_ATTRIBUTION_STRING;
                break;
            case GOOGLE:
                attr = GOOGLE_ATTRIBUTION_STRING;
                break;
            case DEV: default:
                attr = DEV_ATTRIBUTION_STRING;
                break;
        }
        return Response.ok(attr).build();
    }

    @VisibleForTesting
    protected @Nullable InputStream getResourceAsStream(String imageResource) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        return classLoader.getResourceAsStream(imageResource);
    }

    @Override
    public Response getAvailableBackends() {
        Set<String> providers = availableProviders.stream().map(BackendID::getId)
                .collect(Collectors.toSet());
        return Response.ok()
                .entity(new GenericEntity<Set<String>>(providers) {}).build();
    }

    private String getAttributionImageResource(BackendID backendID) {
        if (backendID.equals(BackendID.MS)) {
            return MS_ATTRIBUTION_IMAGE;
        } else if (backendID.equals(BackendID.DEV)) {
            return DEV_ATTRIBUTION_IMAGE;
        } else if (backendID.equals(BackendID.GOOGLE)) {
            return GOOGLE_ATTRIBUTION_IMAGE;
        }
        return "";
    }

    private Optional<APIResponse> validateId(String id) {
        if (isBlank(id)) {
            APIResponse response =
                    new APIResponse(Response.Status.BAD_REQUEST, "Invalid id");
            return Optional.of(response);
        }
        try {
            BackendID.fromString(id);
        } catch (BadRequestException e) {
            APIResponse response =
                    new APIResponse(Response.Status.NOT_FOUND,
                            "Not supported id: " + id);
            return Optional.of(response);
        }

        return Optional.empty();
    }
}
