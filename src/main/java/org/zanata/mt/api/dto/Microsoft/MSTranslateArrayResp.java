package org.zanata.mt.api.dto.Microsoft;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Sample output:
 *
 * <ArrayOfTranslateArrayResponse xmlns="http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2" xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
 *  <TranslateArrayResponse>
 *    <From>language-code</From>
 *    <OriginalTextSentenceLengths xmlns:a="http://schemas.microsoft.com/2003/10/Serialization/Arrays">
 *      <a:int>int-value</a:int>
 *    </OriginalTextSentenceLengths>
 *    <State/>
 *    <TranslatedText>string-value</TranslatedText>
 *    <TranslatedText>string-value</TranslatedText>
 *    <TranslatedTextSentenceLengths xmlns:a="http://schemas.microsoft.com/2003/10/Serialization/Arrays">
 *      <a:int>int-value</a:int>
 *    </TranslatedTextSentenceLengths>
 *  </TranslateArrayResponse>
 * </ArrayOfTranslateArrayResponse>
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@XmlRootElement(name = "ArrayOfTranslateArrayResponse")
public class MSTranslateArrayResp implements Serializable {

    private MSTranslateArrayResponse response;

    @XmlElement(name = "TranslateArrayResponse")
    public MSTranslateArrayResponse getResponse() {
        return response;
    }

    public void setResponse( MSTranslateArrayResponse response) {
        this.response = response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MSTranslateArrayResp)) return false;

        MSTranslateArrayResp resp = (MSTranslateArrayResp) o;

        return response != null ? response.equals(resp.response) :
            resp.response == null;

    }

    @Override
    public int hashCode() {
        return response != null ? response.hashCode() : 0;
    }
}
