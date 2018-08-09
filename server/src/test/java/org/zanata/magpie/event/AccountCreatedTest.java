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

package org.zanata.magpie.event;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.zanata.magpie.model.Role;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class AccountCreatedTest {
    @Test
    public void testConstructor() {
        String email = "test@email.com";
        ImmutableSet<Role> roles = ImmutableSet.of(Role.user);
        AccountCreated accountCreated = new AccountCreated(email, roles);
        assertThat(accountCreated).isNotNull();
        assertThat(accountCreated.getEmail()).isEqualTo(email);
        assertThat(accountCreated.getRoles()).isEqualTo(roles);
    }

    @Test
    public void testEqualHashCode() {
        ImmutableSet<Role> roles = ImmutableSet.of(Role.user);
        AccountCreated accountCreated = new AccountCreated("test@email.com", roles);
        AccountCreated accountCreated2 = new AccountCreated("test@email.com", roles);
        assertThat(accountCreated.equals(accountCreated2)).isTrue();
        assertThat(accountCreated.hashCode()).isEqualTo(accountCreated2.hashCode());

        accountCreated2 = new AccountCreated("test2@email.com", roles);
        assertThat(accountCreated.equals(accountCreated2)).isFalse();
        assertThat(accountCreated.hashCode()).isNotEqualTo(accountCreated2.hashCode());

        accountCreated2 = new AccountCreated("test@email.com", null);
        assertThat(accountCreated.equals(accountCreated2)).isFalse();
        assertThat(accountCreated.hashCode()).isNotEqualTo(accountCreated2.hashCode());
    }
}
