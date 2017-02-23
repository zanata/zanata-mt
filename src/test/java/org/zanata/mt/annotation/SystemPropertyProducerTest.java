package org.zanata.mt.annotation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zanata.mt.api.ArticleTranslatorResource;
import org.zanata.mt.exception.ZanataMTException;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class SystemPropertyProducerTest {
    private SystemPropertyProducer producer;
    private InjectionPoint ip;
    private Annotated annotated;
    private SystemProperty systemProperty;

    @Before
    public void beforeTest() {
        ip = Mockito.mock(InjectionPoint.class);
        annotated = Mockito.mock(Annotated.class);
        systemProperty = Mockito.mock(SystemProperty.class);

        when(ip.getAnnotated()).thenReturn(annotated);
        when(annotated.getAnnotation(SystemProperty.class))
                .thenReturn(systemProperty);

        producer = new SystemPropertyProducer();
    }

    @Test
    public void testFindPropertyNotFound() {
        when(systemProperty.value()).thenReturn("value");
        assertThatThrownBy(() -> producer.findProperty(ip))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testFindProperty() {
        System.setProperty("key", "value");
        when(systemProperty.value()).thenReturn("key");
        assertThat(producer.findProperty(ip)).isEqualTo("value");
    }
}
