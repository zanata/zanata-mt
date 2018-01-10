package org.magpie.mt.exception;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MTException extends RuntimeException {

    public MTException(String message, Throwable e) {
        super(message, e);
    }

    public MTException(String message) {
        super(message);
    }
}
