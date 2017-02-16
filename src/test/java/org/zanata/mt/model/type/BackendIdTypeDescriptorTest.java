package org.zanata.mt.model.type;

import org.hibernate.HibernateException;
import org.junit.Test;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.BackendID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BackendIdTypeDescriptorTest {

    @Test
    public void testConstructor() {
        BackendIdTypeDescriptor descriptor = new BackendIdTypeDescriptor();
        assertThat(descriptor.getJavaTypeClass()).isEqualTo(BackendID.class);
    }

    @Test
    public void testFromString() {
        BackendIdTypeDescriptor descriptor = new BackendIdTypeDescriptor();
        assertThat(descriptor.fromString(null)).isNull();
        assertThat(descriptor.fromString("MS")).isEqualTo(BackendID.MS);
    }

    @Test
    public void testToString() {
        BackendIdTypeDescriptor descriptor = new BackendIdTypeDescriptor();
        assertThat(descriptor.toString(BackendID.MS)).isEqualTo("MS");
    }

    @Test
    public void testUnwrap() {
        BackendIdTypeDescriptor descriptor = new BackendIdTypeDescriptor();
        assertThat(descriptor.unwrap(null, BackendID.class, null)).isNull();

        assertThat(descriptor.unwrap(BackendID.MS, String.class, null))
                .isEqualTo("MS");

        assertThatThrownBy(
                () -> descriptor.unwrap(BackendID.MS, Integer.class, null))
                .isInstanceOf(HibernateException.class);
    }

    @Test
    public void testWrap() {
        BackendIdTypeDescriptor descriptor = new BackendIdTypeDescriptor();
        assertThat(descriptor.wrap(null, null)).isNull();

        assertThat(descriptor.wrap("MS", null)).isEqualTo(BackendID.MS);

        assertThatThrownBy(
                () -> descriptor.wrap(1, null))
                .isInstanceOf(HibernateException.class);
    }
}
