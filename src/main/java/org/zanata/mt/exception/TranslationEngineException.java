package org.zanata.mt.exception;

import java.util.List;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslationEngineException extends Exception {

    public TranslationEngineException(Throwable cause) {
        super(cause);
    }

    public TranslationEngineException(String message, String cause) {
        super("Message:" + message + " Cause:" + cause);
    }
}
