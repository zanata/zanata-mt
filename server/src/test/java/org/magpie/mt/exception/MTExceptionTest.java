package org.magpie.mt.exception;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MTExceptionTest {

    @Test
    public void testExceptionMessage() {
        String message = "error message";
        MTException exception = new MTException(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    public void testExceptionMessageAndThrowable() {
        String message = "error message";
        String cause = "cause of error";
        Throwable t = new Throwable(cause);

        MTException exception = new MTException(message, t);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(t);
    }
}
