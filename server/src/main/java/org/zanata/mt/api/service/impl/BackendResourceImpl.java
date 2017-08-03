package org.zanata.mt.api.service.impl;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.annotation.Credentials;
import org.zanata.mt.api.InputStreamStreamingOutput;
import org.zanata.mt.api.dto.APIResponse;
import org.zanata.mt.api.service.BackendResource;
import org.zanata.mt.backend.google.GoogleCredential;
import org.zanata.mt.model.BackendID;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class BackendResourceImpl implements BackendResource {

    private static final Logger LOG =
            LoggerFactory.getLogger(BackendResourceImpl.class);

    private GoogleCredential googleCredentials;
    private String msCredentials;

    @SuppressWarnings("unused")
    public BackendResourceImpl() {
    }

    @Inject
    public BackendResourceImpl(@Credentials(BackendID.GOOGLE)
            GoogleCredential googleCredentials, @Credentials(BackendID.MS) String msCredentials) {
        this.googleCredentials = googleCredentials;
        this.msCredentials = msCredentials;
    }

    @Override
    public Response getAttribution(@QueryParam("id") String id) {
        Optional<APIResponse> response = validateId(id);
        if (response.isPresent()) {
            return Response.status(response.get().getStatus())
                    .entity(response.get()).build();
        }

        BackendID backendID = BackendID.fromString(id);
        String imageResource = getAttributionImageResource(backendID);
        String docName = new File(imageResource).getName();

        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream(imageResource);
        StreamingOutput output =
                new InputStreamStreamingOutput(is);
        return Response.ok().header("Content-Disposition",
                "attachment; filename=\"" + docName + "\"")
                .entity(output).build();
    }

    @Override
    public Response getAvailableBackends() {
        List<String> providers = Lists.newLinkedList();
        if (googleCredentials.exists()) {
            providers.add(BackendID.GOOGLE.getId());
        }
        if (!isBlank(msCredentials)) {
            providers.add(BackendID.MS.getId());
        }
        providers.add(BackendID.DEV.getId());
        return Response.ok()
                .entity(new GenericEntity<List<String>>(providers) {}).build();
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
        if (!StringUtils.equalsIgnoreCase(id, BackendID.MS.getId()) &&
                !StringUtils.equalsIgnoreCase(id, BackendID.DEV.getId())) {
            APIResponse response =
                    new APIResponse(Response.Status.NOT_FOUND,
                            "Not supported id:" + id);
            return Optional.of(response);
        }
        return Optional.empty();
    }
}
