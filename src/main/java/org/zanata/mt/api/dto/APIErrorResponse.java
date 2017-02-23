package org.zanata.mt.api.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JSON entity for API error response
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.ALWAYS)
public class APIErrorResponse implements Serializable {

    private final static DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ssZ");

    private static final long serialVersionUID = 6040356482529107259L;

    private int status;

    private String title;

    private String details;

    private String timestamp;

    @SuppressWarnings("unused")
    protected APIErrorResponse() {
    }

    public APIErrorResponse(Response.Status status, String title) {
        this(status, null, title);
    }

    public APIErrorResponse(Response.Status status, Exception e, String title) {
        this.status = status.getStatusCode();
        this.title = title;
        if (e != null) {
            this.details = e.getMessage();
        }
        this.timestamp = ZonedDateTime.now().format(DATE_FORMATTER);
    }

    /**
     * The HTTP status code.
     */
    @JsonProperty("status")
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Summary of the problem.
     */
    @JsonProperty("title")
    @NotNull
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Detail explanation for this error.
     */
    @JsonProperty("details")
    @Nullable
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * Timestamp of the response. Format: dd-MM-yyyy HH:mm:ssZ
     */
    @JsonProperty("timestamp")
    @NotNull
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
