package org.zanata.mt.model;

/**
 * Object for extracted value and raw value
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ValueUnit {
    private String value;
    private String rawValue;

    public ValueUnit(String value, String rawValue) {
        this.value = value;
        this.rawValue = rawValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValueUnit)) return false;

        ValueUnit valueUnit = (ValueUnit) o;

        if (value != null ? !value.equals(valueUnit.value) :
            valueUnit.value != null) return false;
        return rawValue != null ? rawValue.equals(valueUnit.rawValue) :
            valueUnit.rawValue == null;

    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (rawValue != null ? rawValue.hashCode() : 0);
        return result;
    }
}
