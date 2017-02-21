package org.zanata.mt.api.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize
public class TypeString implements Serializable {
    private String value;
    private String type;

    @SuppressWarnings("unused")
    public TypeString() {
    }

    public TypeString(String value, String type) {
        this.value = value;
        this.type = type;
    }

    /**
     * @return value of the string
     */
    @JsonProperty("value")
    @NotNull
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return 'text/plain' or 'text/html'
     */
    @JsonProperty("type")
    @NotNull
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TypeString{" +
                "value='" + value + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeString)) return false;

        TypeString that = (TypeString) o;

        if (!getValue().equals(that.getValue()))
            return false;

        return getType().equals(that.getType());

    }

    @Override
    public int hashCode() {
        int result = getValue().hashCode();
        result = 31 * result + getType().hashCode();
        return result;
    }
}
