/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.magpie.dao;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;

import org.zanata.magpie.model.Credential;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class CredentialDAO extends AbstractDAO<Credential> {
    private static final long serialVersionUID = 8592307918827135145L;

    public Optional<Credential> getCredentialByUsername(@Nonnull String username) {
        List<Credential> credentials = getEntityManager()
                .createQuery("from Credential c where c.username = :username",
                        Credential.class).setParameter("username", username)
                .getResultList();
        if (credentials.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(credentials.get(0));
    }
}
