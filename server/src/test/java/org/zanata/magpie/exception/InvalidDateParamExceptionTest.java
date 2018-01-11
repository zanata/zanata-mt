package org.zanata.magpie.exception;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class InvalidDateParamExceptionTest {

    @Test
    public void testMessage() {
        String message = "error message";
        InvalidDateParamException ex = new InvalidDateParamException(message);
        assertThat(ex.getMessage()).isEqualTo(message);
    }
}
