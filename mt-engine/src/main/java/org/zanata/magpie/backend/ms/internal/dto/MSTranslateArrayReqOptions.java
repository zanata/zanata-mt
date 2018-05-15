package org.zanata.magpie.backend.ms.internal.dto;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSTranslateArrayReqOptions implements Serializable {
    private static final String OPTIONS_NAMESPACE = "http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2";

    private static final long serialVersionUID = -2828772281951163409L;

    private String category;
    private String contentType;
    private String reservedFlags;
    private Integer state;
    private String uri;
    private String user;

    @XmlElement(name = "Category",
        namespace = OPTIONS_NAMESPACE)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @XmlElement(name = "ContentType", namespace = OPTIONS_NAMESPACE)
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @XmlElement(name = "ReservedFlags", namespace = OPTIONS_NAMESPACE)
    public String getReservedFlags() {
        return reservedFlags;
    }

    public void setReservedFlags(String reservedFlags) {
        this.reservedFlags = reservedFlags;
    }

    @XmlElement(name = "State", namespace = OPTIONS_NAMESPACE)
    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @XmlElement(name = "Uri", namespace = OPTIONS_NAMESPACE)
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @XmlElement(name = "User", namespace = OPTIONS_NAMESPACE)
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MSTranslateArrayReqOptions options = (MSTranslateArrayReqOptions) o;

        if (category != null ? !category.equals(options.category) :
            options.category != null) return false;
        if (contentType != null ? !contentType.equals(options.contentType) :
            options.contentType != null) return false;
        if (reservedFlags != null ?
            !reservedFlags.equals(options.reservedFlags) :
            options.reservedFlags != null) return false;
        if (state != null ? !state.equals(options.state) :
            options.state != null)
            return false;
        if (uri != null ? !uri.equals(options.uri) : options.uri != null)
            return false;
        return user != null ? user.equals(options.user) : options.user == null;

    }

    @Override
    public int hashCode() {
        int result = category != null ? category.hashCode() : 0;
        result =
            31 * result + (contentType != null ? contentType.hashCode() : 0);
        result =
            31 * result +
                (reservedFlags != null ? reservedFlags.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
