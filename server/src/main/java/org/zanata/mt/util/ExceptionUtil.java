package org.zanata.mt.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.exception.ConstraintViolationException;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class ExceptionUtil {
    @SuppressWarnings("unused")
    private ExceptionUtil() {
    }

    /**
     * Check if throwable consists of {@link ConstraintViolationException}
     */
    public static boolean isConstraintViolationException(Throwable throwable) {
        return ExceptionUtils.indexOfThrowable(throwable,
                ConstraintViolationException.class) != -1;
    }
}
