package org.zanata.magpie.annotation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class EnvVariableProducerTest {
    private EnvVariableProducer producer;
    private InjectionPoint ip;
    private EnvVariable envVariable;

    @Before
    public void beforeTest() {
        ip = Mockito.mock(InjectionPoint.class);
        Annotated annotated = Mockito.mock(Annotated.class);
        envVariable = Mockito.mock(EnvVariable.class);

        when(ip.getAnnotated()).thenReturn(annotated);
        when(annotated.getAnnotation(EnvVariable.class))
                .thenReturn(envVariable);

        producer = new EnvVariableProducer();
    }

    @Test
    public void testFindPropertyNotFound() {
        when(envVariable.value()).thenReturn("value");
        assertThat(producer.findProperty(ip)).isEqualTo("");
    }

    @Test
    public void testFindEnvVariable() {
        String key = "key";
        String value = "value";
        EnvVariableProducer spy = spy(producer);
        when(spy.getEnv(key)).thenReturn(value);
        when(envVariable.value()).thenReturn(key);
        assertThat(spy.findProperty(ip)).isEqualTo(value);
    }
}
