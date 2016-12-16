package org.zanata.mt.annotation;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class SystemPropertyProducer {
    @Produces
    @SystemProperty("")
    String findProperty(InjectionPoint ip) {
        SystemProperty annotation = ip.getAnnotated()
                .getAnnotation(SystemProperty.class);

        String name = annotation.value();
        String found = System.getProperty(name);
        if (found == null) {
            throw new IllegalStateException(
                    "System property '" + name + "' is not defined!");
        }
        return found;
    }
}
