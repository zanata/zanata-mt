package org.magpie.mt.backend.ms.internal.dto;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TextSentenceLength implements Serializable {
    private static final long serialVersionUID = -5609939894237220707L;
    private Integer value;

    public TextSentenceLength(int value) {
        this.value = value;
    }

    public TextSentenceLength() {
    }

    @XmlElement(name = "int")
    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextSentenceLength)) return false;

        TextSentenceLength that = (TextSentenceLength) o;

        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
