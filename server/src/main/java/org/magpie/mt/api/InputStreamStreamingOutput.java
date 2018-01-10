package org.magpie.mt.api;

import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Streaming output that from InputStream for api response
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class InputStreamStreamingOutput implements StreamingOutput {
    private final InputStream input;

    public InputStreamStreamingOutput(@NotNull InputStream input) {
        this.input = input;
    }

    @Override
    public void write(OutputStream output)
            throws IOException, WebApplicationException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }
}
