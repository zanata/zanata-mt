package org.zanata.mt.exception;

import java.util.List;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslationProviderException extends Exception {

    public TranslationProviderException(Throwable cause) {
        super(cause);
    }

    public TranslationProviderException(String message, String cause) {
        super("Message:" + message + " Cause:" + cause);
    }
}
