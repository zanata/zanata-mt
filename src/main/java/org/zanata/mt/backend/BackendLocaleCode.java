package org.zanata.mt.backend;

import java.io.Serializable;

/**
 * Interface for locale code object used in backend package.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface BackendLocaleCode extends Serializable {

    String getLocaleCode();
}
