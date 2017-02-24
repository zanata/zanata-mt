package org.zanata.mt.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DomUtilTest {

    @Test
    public void privateNotesAreRecognised() {
        String html = "<div id='private-notes-testing'></div>";
        assertThat(DomUtil.isKCSPrivateNotes(html)).isTrue();
    }

    @Test
    public void nonPrivateNotesAreRecognised() {
        String html = "<div id='testing-private-notes'></div>";
        assertThat(DomUtil.isKCSPrivateNotes(html)).isFalse();
    }

    @Test
    public void codeSectionRecognised() {
        String html = "<div id='code-raw'></div>";
        assertThat(DomUtil.isKCSCodeSection(html)).isTrue();
    }

    @Test
    public void nonCodeSectionRecognised() {
        String html = "<div id='not-code-raw'></div>";
        assertThat(DomUtil.isKCSCodeSection(html)).isFalse();
    }

    @Test
    public void isNonTranslatableNodeRecognised() {
        String html = "<div id='code-raw' translate='no'></div>";
        assertThat(DomUtil.isNonTranslatableNode(html)).isTrue();
    }

    @Test
    public void translatableNodeRecognised() {
        String html = "<div id='code-raw' translate='yes'></div>";
        assertThat(DomUtil.isNonTranslatableNode(html)).isFalse();
    }

    @Test
    public void translatableNodeRecognised2() {
        String html = "<div id='code-raw'></div>";
        assertThat(DomUtil.isNonTranslatableNode(html)).isFalse();
    }
}
