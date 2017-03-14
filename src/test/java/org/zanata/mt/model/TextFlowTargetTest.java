package org.zanata.mt.model;

import org.junit.Test;
import org.zanata.mt.api.dto.LocaleId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TextFlowTargetTest {

    @Test
    public void testEmptyConstructor() {
        TextFlowTarget target = new TextFlowTarget();
        assertThat(target.getContent()).isNull();
    }

    @Test
    public void testConstructor() {
        TextFlow textFlow = new TextFlow();
        Locale locale = new Locale(LocaleId.EN_US, "English US");

        TextFlowTarget target =
                new TextFlowTarget("content", "raw content", textFlow, locale,
                        BackendID.MS);

        assertThat(target.getContent()).isEqualTo("content");
        assertThat(target.getRawContent()).isEqualTo("raw content");
        assertThat(target.getTextFlow()).isEqualTo(textFlow);
        assertThat(target.getLocale()).isEqualTo(locale);
        assertThat(target.getBackendId()).isEqualTo(BackendID.MS);
        assertThat(target.getUsedCount()).isEqualTo(0);
    }

    @Test
    public void testIncrementCount() {
        TextFlowTarget target = new TextFlowTarget();
        assertThat(target.getUsedCount()).isEqualTo(0);
        target.incrementCount();
        assertThat(target.getUsedCount()).isEqualTo(1);
    }

    @Test
    public void testEqualsAndHashcode() {
        Locale srcLang = new Locale(LocaleId.EN_US, "English US");
        Locale transLang = new Locale(LocaleId.DE, "German");
        TextFlow textFlow = new TextFlow("content", srcLang);

        TextFlowTarget target1 =
                new TextFlowTarget("content", "raw content", textFlow, transLang,
                        BackendID.MS);

        TextFlowTarget target2 =
                new TextFlowTarget("content", "raw content", textFlow, transLang,
                        BackendID.MS);

        assertThat(target1.hashCode()).isEqualTo(target2.hashCode());
        assertThat(target1.equals(target2)).isTrue();

        // diff text flow
        TextFlow newTextFlow = new TextFlow("new content", srcLang);
        target2 =
                new TextFlowTarget("content", "raw content", newTextFlow, transLang,
                        BackendID.MS);
        assertThat(target1.hashCode()).isNotEqualTo(target2.hashCode());
        assertThat(target1.equals(target2)).isFalse();


        // diff locale
        Locale newTransLang = new Locale(LocaleId.FR, "French");
        target2 =
                new TextFlowTarget("content", "raw content", textFlow, newTransLang,
                        BackendID.MS);
        assertThat(target1.hashCode()).isNotEqualTo(target2.hashCode());
        assertThat(target1.equals(target2)).isFalse();

        // diff type
        String test = "test";
        assertThat(target1.hashCode()).isNotEqualTo(test.hashCode());
        assertThat(target1.equals(test)).isFalse();
    }
}
