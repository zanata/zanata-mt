package org.zanata.magpie.backend.ms.internal.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Sample output:
 *
 * <ArrayOfTranslateArray2Response xmlns="http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2" xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
 *     <TranslateArray2Response>
 *         <Alignment/>
 *         <From>en</From>
 *         <OriginalTextSentenceLengths xmlns:a="http://schemas.microsoft.com/2003/10/Serialization/Arrays">
 *             <a:int>71</a:int>
 *         </OriginalTextSentenceLengths>
 *         <TranslatedText>Panique du noyau à trace_find_cmdline() fonction - portail client de Red Hat</TranslatedText>
 *         <TranslatedTextSentenceLengths xmlns:a="http://schemas.microsoft.com/2003/10/Serialization/Arrays">
 *             <a:int>76</a:int>
 *         </TranslatedTextSentenceLengths>
 *     </TranslateArray2Response>
 * </ArrayOfTranslateArray2Response>
 *
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@XmlRootElement(name = "ArrayOfTranslateArray2Response")
public class MSTranslateArrayResp implements Serializable {

    private static final long serialVersionUID = -4056600957155750604L;

    private List<MSTranslateArrayResponse> response = new ArrayList<>();

    @XmlElement(name = "TranslateArray2Response")
    public List<MSTranslateArrayResponse> getResponse() {
        return response;
    }

    public void setResponse(List<MSTranslateArrayResponse> response) {
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
