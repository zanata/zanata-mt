package org.zanata.mt.backend.ms.internal.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.api.dto.LocaleIdAdapter;

/**
 * https://msdn.microsoft.com/en-us/library/ff512422.aspx
 * Sample xml:
 *
 * <TranslateArrayRequest>
 *  <AppId />
 *  <From>language-code</From>
 *  <Options>
 *    <Category xmlns="http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2" >string-value</Category>
 *    <ContentType xmlns="http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2">text/plain</ContentType>
 *    <ReservedFlags xmlns="http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2" />
 *    <State xmlns="http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2" >int-value</State>
 *    <Uri xmlns="http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2" >string-value</Uri>
 *    <User xmlns="http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2" >string-value</User>
 *  </Options>
 *  <Texts>
 *    <string xmlns="http://schemas.microsoft.com/2003/10/Serialization/Arrays">string-value</string>
 *    <string xmlns="http://schemas.microsoft.com/2003/10/Serialization/Arrays">string-value</string>
 *  </Texts>
 *  <To>language-code</To>
 * </TranslateArrayRequest>
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@XmlRootElement(name = "TranslateArrayRequest")
@XmlType(propOrder = {"appId", "srcLanguage", "options", "texts", "transLanguage"})
public class MSTranslateArrayReq implements Serializable {
    private static final long serialVersionUID = 3821282850166291221L;

    private String appId;
    private LocaleId srcLanguage;
    private MSTranslateArrayReqOptions options;
    private List<MSString> texts = new ArrayList<>();
    private LocaleId transLanguage;

    public MSTranslateArrayReq() {
    }

    @XmlElement(name = "AppId", nillable = true)
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @XmlElement(name = "From", required = true)
    @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
    public LocaleId getSrcLanguage() {
        return srcLanguage;
    }

    public void setSrcLanguage(LocaleId srcLanguage) {
        this.srcLanguage = srcLanguage;
    }

    @XmlElement(name = "Options")
    public MSTranslateArrayReqOptions getOptions() {
        return options;
    }

    public void setOptions(MSTranslateArrayReqOptions options) {
        this.options = options;
    }

    @XmlElementWrapper(name = "Texts", required = true)
    @XmlElement(name = "string", namespace = "http://schemas.microsoft.com/2003/10/Serialization/Arrays")
    public List<MSString> getTexts() {
        return texts;
    }

    public void setTexts(List<MSString> texts) {
        this.texts = texts;
    }

    @XmlElement(name = "To", required = true)
    @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
    public LocaleId getTransLanguage() {
        return transLanguage;
    }

    public void setTransLanguage(LocaleId transLanguage) {
        this.transLanguage = transLanguage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MSTranslateArrayReq that = (MSTranslateArrayReq) o;

        if (appId != null ? !appId.equals(that.appId) : that.appId != null)
            return false;
        if (srcLanguage != null ? !srcLanguage.equals(that.srcLanguage) :
            that.srcLanguage != null) return false;
        if (options != null ? !options.equals(that.options) :
            that.options != null)
            return false;
        if (transLanguage != null ? !transLanguage.equals(that.transLanguage) :
            that.transLanguage != null) return false;
        return texts != null ? texts.equals(that.texts) : that.texts == null;

    }

    @Override
    public int hashCode() {
        int result = appId != null ? appId.hashCode() : 0;
        result =
            31 * result + (srcLanguage != null ? srcLanguage.hashCode() : 0);
        result = 31 * result + (options != null ? options.hashCode() : 0);
        result =
            31 * result +
                (transLanguage != null ? transLanguage.hashCode() : 0);
        result = 31 * result + (texts != null ? texts.hashCode() : 0);
        return result;
    }
}
