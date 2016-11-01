package org.zanata.mt.exception;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BadTranslationRequestException extends Exception {

    public BadTranslationRequestException() {
        super("Invalid value in request");
    }
}
