package org.zanata.mt.api.dto;

import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class APIErrorResponse implements Serializable {
    private final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("dd-MM-yyyy HH:mm:ssZ");

    /**
     * The HTTP status code.
     */
    private String status;

    /**
     * Summary of the problem.
     */
    private String title;

    /**
     * Detail explanation for this error.
     */
    private String details;

    /**
     * Meta data for response. e.g. timestamp
     */
    private String timestamp;

    public APIErrorResponse() {
    }

    public APIErrorResponse(Response.Status status, String title) {
        this(status, null, title);
    }

    public APIErrorResponse(Response.Status status, Exception e, String title) {
        this.status = String.valueOf(status.getStatusCode());
        this.title = title;
        if (e != null) {
            this.details = e.getMessage();
        }
        this.timestamp = DATE_FORMAT.format(new Date());
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
