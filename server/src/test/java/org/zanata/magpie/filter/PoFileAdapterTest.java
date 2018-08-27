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

package org.zanata.magpie.filter;

import org.apache.commons.lang3.tuple.Pair;
import org.fedorahosted.tennera.jgettext.Message;
import org.junit.Test;
import org.mockito.Mockito;
import org.zanata.magpie.api.dto.DocumentContent;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.api.dto.TypeString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class PoFileAdapterTest {
    private PoFileAdapter poFileAdapter = new PoFileAdapter(StandardCharsets.UTF_8);

    @Test
    public void testGetTranslationFileExtension() {
        assertThat(poFileAdapter.getTranslationFileExtension()).isNotBlank()
            .isEqualTo("po");
    }

    @Test
    public void testParseDocument() throws FileNotFoundException {
        final int entriesCount = 5;
        File sourceFile =
            new File("src/test/resources/test.pot");
        InputStream inputStream = new FileInputStream(sourceFile);

        LocaleCode fromLocaleCode = LocaleCode.EN;
        String filename = "test.pot";
        Pair<DocumentContent, Map<String, Message>> contents =
            poFileAdapter.parseSourceDocument(inputStream, filename, fromLocaleCode);
        assertThat(contents.getLeft()).isNotNull();
        assertThat(contents.getLeft().getUrl()).isEqualTo(filename);
        assertThat(contents.getLeft().getLocaleCode()).isEqualTo(fromLocaleCode.getId());
        assertThat(contents.getLeft().getContents()).hasSize(entriesCount);

        assertThat(contents.getRight()).isNotEmpty();
    }

    @Test
    public void testWriteTranslatedFile() throws IOException {
        File sourceFile =
            new File("src/test/resources/test.pot");
        InputStream inputStream = new FileInputStream(sourceFile);

        LocaleCode fromLocaleCode = LocaleCode.EN;
        LocaleCode toLocaleCode = LocaleCode.DE;
        String attribution = "translated by test";

        String filename = "test.pot";
        Pair<DocumentContent, Map<String, Message>> contents =
            poFileAdapter.parseSourceDocument(inputStream, filename, fromLocaleCode);

        // modify source string to be translated
        for (TypeString typeString: contents.getLeft().getContents()) {
            typeString.setValue(typeString.getValue() + "_translated");
        }

        OutputStream outputStream = Mockito.mock(OutputStream.class);
        poFileAdapter
            .writeTranslatedFile(outputStream, fromLocaleCode, toLocaleCode,
                contents.getLeft(), contents.getRight(), attribution);
        verify(outputStream, atLeastOnce())
            .write(any(byte[].class), anyInt(), anyInt());
    }
}
