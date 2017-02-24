package org.zanata.mt.api.dto;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocumentContentTest {

    @Test
    public void testEmptyConstructor() {
        DocumentContent docContent = new DocumentContent();
    }

    @Test
    public void testConstructor() {
        List<TypeString> lists = Lists.newArrayList(
                new TypeString("test", "text/plain", "meta"));
        DocumentContent
                docContent = new DocumentContent(lists, "http://localhost", "en");

        assertThat(docContent.getBackendId()).isNull();
        assertThat(docContent.getContents()).isEqualTo(lists);
        assertThat(docContent.getLocale()).isEqualTo("en");
        assertThat(docContent.getUrl()).isEqualTo("http://localhost");
    }

    @Test
    public void testConstructor2() {
        List<TypeString> lists = Lists.newArrayList(
                new TypeString("test", "text/plain", "meta"));
        DocumentContent
                docContent = new DocumentContent(lists, "http://localhost", "en", "backendId");

        assertThat(docContent.getContents()).isEqualTo(lists);
        assertThat(docContent.getLocale()).isEqualTo("en");
        assertThat(docContent.getUrl()).isEqualTo("http://localhost");
        assertThat(docContent.getBackendId()).isEqualTo("backendId");
    }

    @Test
    public void testUrl() {
        DocumentContent docContent = new DocumentContent();
        docContent.setUrl("http://localhost");
        assertThat(docContent.getUrl()).isEqualTo("http://localhost");
   }

    @Test
    public void testContents() {
        DocumentContent docContent = new DocumentContent();
        List<TypeString> lists =
                Lists.newArrayList(
                        new TypeString("test", "text/plain", "meta"));
        docContent.setContents(lists);
        assertThat(docContent.getContents()).isEqualTo(lists);
    }

    @Test
    public void testLocale() {
        DocumentContent docContent = new DocumentContent();
        docContent.setLocale("en");
        assertThat(docContent.getLocale()).isEqualTo("en");
    }

    @Test
    public void testBackendId() {
        DocumentContent docContent = new DocumentContent();
        docContent.setBackendId("backendId");
        assertThat(docContent.getBackendId()).isEqualTo("backendId");
    }

    @Test
    public void testEqualsAndHashcode() {
        DocumentContent docContent1 = getDefaultDocContent();

        // change backend id
        DocumentContent docContent2 = getDefaultDocContent();
        docContent2.setBackendId("backendId2");

        assertThat(docContent1.equals(docContent2)).isFalse();
        assertThat(docContent1.hashCode()).isNotEqualTo(docContent2.hashCode());

        // change locale
        docContent2 = getDefaultDocContent();
        docContent2.setLocale("fr");

        assertThat(docContent1.equals(docContent2)).isFalse();
        assertThat(docContent1.hashCode()).isNotEqualTo(docContent2.hashCode());

        // change url
        docContent2 = getDefaultDocContent();
        docContent2.setUrl("http://localhost2");

        assertThat(docContent1.equals(docContent2)).isFalse();
        assertThat(docContent1.hashCode()).isNotEqualTo(docContent2.hashCode());

        // change contents
        docContent2 = getDefaultDocContent();
        docContent2.setContents(
                Lists.newArrayList(
                        new TypeString("test2", "text/plain", "meta")));

        assertThat(docContent1.equals(docContent2)).isFalse();
        assertThat(docContent1.hashCode()).isNotEqualTo(docContent2.hashCode());

        docContent2 = getDefaultDocContent();

        assertThat(docContent1.equals(docContent2)).isTrue();
        assertThat(docContent1.hashCode()).isEqualTo(docContent2.hashCode());
    }

    private DocumentContent getDefaultDocContent() {
        List<TypeString> lists =
                Lists.newArrayList(
                        new TypeString("test", "text/plain", "meta"));
        return new DocumentContent(lists, "http://localhost", "en", "backendId");
    }
}
