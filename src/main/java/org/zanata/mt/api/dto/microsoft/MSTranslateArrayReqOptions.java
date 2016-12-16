package org.zanata.mt.api.dto.microsoft;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;

import org.zanata.mt.service.impl.MicrosoftTranslatorAPI;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSTranslateArrayReqOptions implements Serializable {

    private static final long serialVersionUID = -2828772281951163409L;

    private String category;
    private String contentType;
    private String reservedFlags;
    private Integer state;
    private String uri;
    private String user;

    @XmlElement(name = "Category",
        namespace = MicrosoftTranslatorAPI.OPTIONS_NAMESPACE)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @XmlElement(name = "ContentType",
            namespace = MicrosoftTranslatorAPI.OPTIONS_NAMESPACE)
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @XmlElement(name = "ReservedFlags",
        namespace = MicrosoftTranslatorAPI.OPTIONS_NAMESPACE)
    public String getReservedFlags() {
        return reservedFlags;
    }

    public void setReservedFlags(String reservedFlags) {
        this.reservedFlags = reservedFlags;
    }

    @XmlElement(name = "State",
        namespace = MicrosoftTranslatorAPI.OPTIONS_NAMESPACE)
    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @XmlElement(name = "Uri",
        namespace = MicrosoftTranslatorAPI.OPTIONS_NAMESPACE)
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @XmlElement(name = "User",
        namespace = MicrosoftTranslatorAPI.OPTIONS_NAMESPACE)
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
