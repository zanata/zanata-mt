package org.zanata.mt.annotation;

import org.junit.Test;
import org.mockito.Mockito;
import org.zanata.mt.exception.ZanataMTException;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class SystemPropertyProducerTest {

    @Test
    public void testConstructor() {
        SystemPropertyProducer producer = new SystemPropertyProducer();

    }

    @Test
    public void testFindPropertyNotFound() {
        SystemPropertyProducer producer = new SystemPropertyProducer();
        InjectionPoint ip = Mockito.mock(InjectionPoint.class);
        Annotated annotated = Mockito.mock(Annotated.class);
        SystemProperty systemProperty = Mockito.mock(SystemProperty.class);

        when(ip.getAnnotated()).thenReturn(annotated);
        when(annotated.getAnnotation(SystemProperty.class))
                .thenReturn(systemProperty);

        when(systemProperty.value()).thenReturn("value");

        assertThatThrownBy(() -> producer.findProperty(ip))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testFindProperty() {
        System.setProperty("key", "value");
        SystemPropertyProducer producer = new SystemPropertyProducer();
        InjectionPoint ip = Mockito.mock(InjectionPoint.class);
        Annotated annotated = Mockito.mock(Annotated.class);
        SystemProperty systemProperty = Mockito.mock(SystemProperty.class);

        when(ip.getAnnotated()).thenReturn(annotated);
        when(annotated.getAnnotation(SystemProperty.class))
                .thenReturn(systemProperty);

        when(systemProperty.value()).thenReturn("key");

        assertThat(producer.findProperty(ip)).isEqualTo("value");
    }
}
