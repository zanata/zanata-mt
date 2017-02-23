package org.zanata.mt.api.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * JSON entity for source and translation string with specified {@link #type}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize
public class TypeString implements Serializable {
    private String value;
    private String type;
    private String metadata;

    @SuppressWarnings("unused")
    public TypeString() {
    }

    public TypeString(String value, String type, String metadata) {
        this.value = value;
        this.type = type;
        this.metadata = metadata;
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

    /**
     * @return metadata of the entry
     */
    @JsonProperty("metadata")
    @Nullable
    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "TypeString{" +
                "value='" + value + '\'' +
                ", type='" + type + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeString)) return false;

        TypeString that = (TypeString) o;

        if (getValue() != null ? !getValue().equals(that.getValue()) :
                that.getValue() != null) return false;
        if (getType() != null ? !getType().equals(that.getType()) :
                that.getType() != null) return false;
        return getMetadata() != null ?
                getMetadata().equals(that.getMetadata()) :
                that.getMetadata() == null;
    }

    @Override
    public int hashCode() {
        int result = getValue() != null ? getValue().hashCode() : 0;
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result =
                31 * result +
                        (getMetadata() != null ? getMetadata().hashCode() : 0);
        return result;
    }
}
