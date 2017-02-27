package org.zanata.mt.api.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.zanata.mt.api.InputStreamStreamingOutput;
import org.zanata.mt.api.dto.APIErrorResponse;
import org.zanata.mt.api.service.BackendResource;
import org.zanata.mt.model.BackendID;

import javax.enterprise.context.RequestScoped;
import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class BackendResourceImpl implements BackendResource {

    private static final String MS_ATTRIBUTION_IMAGE = "/images/MS_attribution.png";

    @SuppressWarnings("unused")
    public BackendResourceImpl() {
    }

    public Response getAttribution(@NotNull @QueryParam("id") String id) {
        Optional<APIErrorResponse> response = validateId(id);
        if (response.isPresent()) {
            return Response.status(response.get().getStatus())
                    .entity(response.get()).build();
        }

        BackendID backendID = new BackendID(id.toUpperCase());
        String imageResource = getAttributionImageResource(backendID);
        String docName = id + "-attribution.png";

        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream(imageResource);
        StreamingOutput output =
                new InputStreamStreamingOutput(is);
        return Response.ok().header("Content-Disposition",
                "attachment; filename=\"" + docName + "\"")
                .entity(output).build();
    }

    private String getAttributionImageResource(BackendID backendID) {
        if (backendID.equals(BackendID.MS)) {
            return MS_ATTRIBUTION_IMAGE;
        }
        return "";
    }

    private Optional<APIErrorResponse> validateId(String id) {
        if (StringUtils.isBlank(id)) {
            APIErrorResponse response =
                    new APIErrorResponse(Response.Status.BAD_REQUEST, "Invalid id");
            return Optional.of(response);
        }
        if (!StringUtils.equalsIgnoreCase(id, BackendID.MS.getId())) {
            APIErrorResponse response =
                    new APIErrorResponse(Response.Status.NOT_FOUND, "Not supported id:" + id);
            return Optional.of(response);
        }
        return Optional.empty();
    }
}
