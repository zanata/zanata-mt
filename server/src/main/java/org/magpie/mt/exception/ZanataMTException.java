package org.magpie.mt.exception;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ZanataMTException extends RuntimeException {

    public ZanataMTException(String message, Throwable e) {
        super(message, e);
    }

    public ZanataMTException(String message) {
        super(message);
    }
}
