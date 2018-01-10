package org.magpie.mt;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ApplicationPath;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ZanataMTApplicationTest {

    private ZanataMTApplication zanataMTApplication;

    @Before
    public void setup() {
        zanataMTApplication = new ZanataMTApplication();
    }

    @Test
    public void testApplicationPath() {
        Class clazz = zanataMTApplication.getClass();
        assertThat(clazz.isAnnotationPresent(ApplicationPath.class)).isTrue();

        ApplicationPath annotation =
                (ApplicationPath) clazz.getAnnotation(ApplicationPath.class);
        assertThat(annotation.value()).isEqualTo("api");
    }

    @Test
    public void testGetClasses() {
        Set<Class<?>> classes = zanataMTApplication.getClasses();
        assertThat(classes).isNotEmpty();
    }
}
