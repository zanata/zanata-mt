package org.zanata.magpie.backend.ms.internal.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSString implements Serializable {
    private static final long serialVersionUID = 7188269066576053950L;

    private String text;

    public MSString() {
        this(null);
    }

    public MSString(String text) {
        this.text = text;
    }

    @JsonProperty("Text")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MSString)) return false;

        MSString msString = (MSString) o;

        return text != null ? text.equals(msString.text) :
            msString.text == null;

    }

    @Override
    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }
}
