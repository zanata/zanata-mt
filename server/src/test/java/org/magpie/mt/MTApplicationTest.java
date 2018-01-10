package org.magpie.mt;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ApplicationPath;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MTApplicationTest {

    private MTApplication MTApplication;

    @Before
    public void setup() {
        MTApplication = new MTApplication();
    }

    @Test
    public void testApplicationPath() {
        Class clazz = MTApplication.getClass();
        assertThat(clazz.isAnnotationPresent(ApplicationPath.class)).isTrue();

        ApplicationPath annotation =
                (ApplicationPath) clazz.getAnnotation(ApplicationPath.class);
        assertThat(annotation.value()).isEqualTo("api");
    }

    @Test
    public void testGetClasses() {
        Set<Class<?>> classes = MTApplication.getClasses();
        assertThat(classes).isNotEmpty();
    }
}
