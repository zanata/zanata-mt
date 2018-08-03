package org.zanata.magpie.api.dto;


import java.io.Serializable;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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

    @SuppressWarnings({"unused", "initialization"})
    public TypeString() {
    }

    public TypeString(String value, String type, String metadata) {
        this.value = value;
        this.type = type;
        this.metadata = metadata;
    }

    /**
     * value of the string
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
     * 'text/plain', 'text/html' or 'text/xml'
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
     * metadata of the entry
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
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeString)) return false;

        TypeString that = (TypeString) o;

        if (!getValue().equals(that.getValue())) {
            return false;
        }
        if (!getType().equals(that.getType())) {
            return false;
        }
        String thisMetadata = getMetadata();
        return thisMetadata != null ?
                thisMetadata.equals(that.getMetadata()) :
                that.getMetadata() == null;
    }

    @Override
    public int hashCode() {
        int result = getValue().hashCode();
        result = 31 * result + getType().hashCode();
        result =
                31 * result +
                        (getMetadata() != null ? getMetadata().hashCode() : 0);
        return result;
    }
}
