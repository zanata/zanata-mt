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
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class PoFilterTest {
    private PoFilter filter = new PoFilter(StandardCharsets.UTF_8);

    @Test
    public void testGetTranslationFileExtension() {
        assertThat(filter.getTranslationFileExtension()).isNotBlank()
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
        DocumentContent documentContent =
            filter.parseDocument(inputStream, filename, fromLocaleCode);
        assertThat(documentContent).isNotNull();
        assertThat(documentContent.getUrl()).isEqualTo(filename);
        assertThat(documentContent.getLocaleCode()).isEqualTo(fromLocaleCode.getId());
        assertThat(documentContent.getContents()).hasSize(entriesCount);
    }

    @Test
    public void testWriteTranslatedFileBeforeParse() throws IOException {
        OutputStream outputStream = Mockito.mock(OutputStream.class);
        LocaleCode fromLocaleCode = LocaleCode.EN;
        LocaleCode toLocaleCode = LocaleCode.DE;
        List<TypeString> contents = new ArrayList<>();
        contents.add(new TypeString("testing", "text/plain", null));
        DocumentContent translatedDocContent = new DocumentContent(contents, "testing", toLocaleCode.getId());

        filter.writeTranslatedFile(outputStream, fromLocaleCode, toLocaleCode,
            translatedDocContent);
        verifyZeroInteractions(outputStream);
    }

    @Test
    public void testWriteTranslatedFile() throws IOException {
        final int entriesCount = 5;
        File sourceFile =
            new File("src/test/resources/test.pot");
        InputStream inputStream = new FileInputStream(sourceFile);

        LocaleCode fromLocaleCode = LocaleCode.EN;
        LocaleCode toLocaleCode = LocaleCode.DE;

        String filename = "test.pot";
        DocumentContent documentContent =
            filter.parseDocument(inputStream, filename, fromLocaleCode);

        // modify source string to be translated
        for (TypeString typeString: documentContent.getContents()) {
            typeString.setValue(typeString.getValue() + "_translated");
        }

        OutputStream outputStream = Mockito.mock(OutputStream.class);
        filter.writeTranslatedFile(outputStream, fromLocaleCode, toLocaleCode,
            documentContent);
        verify(outputStream, times(entriesCount + 1))
            .write(any(byte[].class), anyInt(), anyInt());
    }

    @Test
    public void testWriteMsgstrPlurals() throws IOException {
        Writer writer = Mockito.mock(Writer.class);
        String prefix = "<<";
        List<String> strings = new ArrayList<>();
        strings.add("test1");
        strings.add("test2");
        filter.writeMsgstrPlurals(prefix, strings, writer);
        verify(writer, times(4)).write(anyString());
    }
}
