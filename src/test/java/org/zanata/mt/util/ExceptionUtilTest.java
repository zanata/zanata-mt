package org.zanata.mt.util;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.zanata.mt.exception.ZanataMTException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ExceptionUtilTest {

    @Test
    public void testIsNotConstraintViolationException() {
        ZanataMTException e = new ZanataMTException("test");
        assertThat(ExceptionUtil.isConstraintViolationException(e)).isFalse();
    }

    @Test
    public void testIsConstraintViolationException() {
        ConstraintViolationException e =
                new ConstraintViolationException("test", null, null);
        assertThat(ExceptionUtil.isConstraintViolationException(e)).isTrue();
    }
}
