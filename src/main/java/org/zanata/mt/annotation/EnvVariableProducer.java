package org.zanata.mt.annotation;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class EnvVariableProducer {
    @Produces
    @EnvVariable("")
    String findProperty(InjectionPoint ip) {
        EnvVariable annotation = ip.getAnnotated()
                .getAnnotation(EnvVariable.class);

        String name = annotation.value();
        String found = getEnv(name);
        if (found == null) {
            throw new IllegalStateException(
                    "Environment variable '" + name + "' is not defined!");
        }
        return found;
    }

    String getEnv(String name) {
        return System.getenv(name);
    }
}
