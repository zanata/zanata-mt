package org.zanata.magpie.backend.ms.internal.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The body of the request is a JSON array. Each array element is a JSON object
 * with a string property named Text, which represents the string to translate.
 *
 * JSON
 * [
 *     {"Text":"I would really like to drive your car around the block a few times."}
 * ]
 * The following limitations apply:
 *
 * The array can have at most 25 elements.
 * The entire text included in the request cannot exceed 5,000 characters including spaces.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSTranslateArrayReq implements Serializable {
    private static final long serialVersionUID = 3821282850166291221L;

    private List<MSString> texts = new ArrayList<>();

    public MSTranslateArrayReq() {
    }

    public List<MSString> getTexts() {
        return texts;
    }

    public void setTexts(List<MSString> texts) {
        this.texts = texts;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MSTranslateArrayReq)) return false;
        MSTranslateArrayReq that = (MSTranslateArrayReq) o;
        return Objects.equals(getTexts(), that.getTexts());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getTexts());
    }
}
