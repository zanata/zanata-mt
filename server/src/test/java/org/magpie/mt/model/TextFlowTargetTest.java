package org.magpie.mt.model;

import org.junit.Test;
import org.magpie.mt.api.dto.LocaleCode;

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
        Locale locale = new Locale(LocaleCode.EN_US, "English US");

        TextFlowTarget target =
                new TextFlowTarget("content", "raw content", textFlow, locale,
                        BackendID.MS);

        assertThat(target.getContent()).isEqualTo("content");
        assertThat(target.getRawContent()).isEqualTo("raw content");
        assertThat(target.getTextFlow()).isEqualTo(textFlow);
        assertThat(target.getLocale()).isEqualTo(locale);
        assertThat(target.getBackendId()).isEqualTo(BackendID.MS);
    }

    @Test
    public void testUpdateContent() {
        TextFlow textFlow = new TextFlow();
        Locale locale = new Locale(LocaleCode.EN_US, "English US");

        TextFlowTarget tft =
                new TextFlowTarget("content", "raw content", textFlow, locale,
                        BackendID.MS);

        tft.updateContent("new content", "new raw content");
        assertThat(tft.getContent()).isEqualTo("new content");
        assertThat(tft.getRawContent()).isEqualTo("new raw content");
    }

    @Test
    public void testEqualsAndHashcode() {
        Locale fromLocale = new Locale(LocaleCode.EN_US, "English US");
        Locale toLocale = new Locale(LocaleCode.DE, "German");
        TextFlow textFlow = new TextFlow(new Document(), "content", fromLocale);

        TextFlowTarget target1 =
                new TextFlowTarget("content", "raw content", textFlow, toLocale,
                        BackendID.MS);

        TextFlowTarget target2 =
                new TextFlowTarget("content", "raw content", textFlow, toLocale,
                        BackendID.MS);

        assertThat(target1.hashCode()).isEqualTo(target2.hashCode());
        assertThat(target1.equals(target2)).isTrue();

        // diff text flow
        TextFlow newTextFlow = new TextFlow(new Document(), "new content", fromLocale);
        target2 =
                new TextFlowTarget("content", "raw content", newTextFlow, toLocale,
                        BackendID.MS);
        assertThat(target1.hashCode()).isNotEqualTo(target2.hashCode());
        assertThat(target1.equals(target2)).isFalse();


        // diff locale
        Locale newToLocale = new Locale(LocaleCode.FR, "French");
        target2 =
                new TextFlowTarget("content", "raw content", textFlow, newToLocale,
                        BackendID.MS);
        assertThat(target1.hashCode()).isNotEqualTo(target2.hashCode());
        assertThat(target1.equals(target2)).isFalse();

        // diff type
        String test = "test";
        assertThat(target1.hashCode()).isNotEqualTo(test.hashCode());
        assertThat(target1.equals(test)).isFalse();
    }
}
