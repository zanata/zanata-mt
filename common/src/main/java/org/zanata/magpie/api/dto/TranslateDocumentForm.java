package org.zanata.magpie.api.dto;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;
import java.io.InputStream;
import java.io.Serializable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslateDocumentForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @FormParam("file")
    @PartType("application/octet-stream")
    private transient InputStream fileStream;

    @FormParam("fileName")
    @PartType("text/plain")
    private String fileName;

    @FormParam("type")
    @PartType("text/plain")
    private String type;

    public InputStream getFileStream() {
        return fileStream;
    }

    public void setFileStream(InputStream fileStream) {
        this.fileStream = fileStream;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
