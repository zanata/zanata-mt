package org.zanata.mt.api;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class InputStreamStreamingOutputTest {

    @Test
    public void testWrite() throws IOException {
        InputStream is = Mockito.mock(InputStream.class);
        OutputStream out = Mockito.mock(OutputStream.class);
        when(is.read(any())).thenReturn(-1);
        InputStreamStreamingOutput streamingOutput = new InputStreamStreamingOutput(is);
        streamingOutput.write(out);
        verifyNoMoreInteractions(out);
    }

    @Test
    public void testWrite2() throws IOException {
        String text = "text to write";
        InputStream is = new ByteArrayInputStream(text.getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStreamStreamingOutput streamingOutput = new InputStreamStreamingOutput(is);
        streamingOutput.write(out);
        out.toString();
        assertThat(out.toString()).isEqualTo(text);
    }
}
