package org.zanata.magpie.exception;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class DataConstraintViolationException extends RuntimeException {
    private static final long serialVersionUID = -4452992947104970545L;

    public DataConstraintViolationException(Throwable e) {
        super(e);
    }
}
