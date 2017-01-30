package org.zanata.mt.backend.ms.internal.dto;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TranslateArray2Response")
public class MSTranslateArrayResponse implements Serializable {
    private static final long serialVersionUID = -6479498157835672314L;
    private String srcLanguage;
    private String alignment;
    private TextSentenceLength originalTextSentenceLengths;
    private TextSentenceLength translatedTextSentenceLengths;
    private MSString translatedText;

    @XmlElement(name = "From")
    public String getSrcLanguage() {
        return srcLanguage;
    }

    public void setSrcLanguage(String srcLanguage) {
        this.srcLanguage = srcLanguage;
    }

    @XmlElement(name = "Alignment")
    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    @XmlElement(name = "OriginalTextSentenceLengths")
    public TextSentenceLength getOriginalTextSentenceLengths() {
        return originalTextSentenceLengths;
    }

    public void setOriginalTextSentenceLengths(
        TextSentenceLength originalTextSentenceLengths) {
        this.originalTextSentenceLengths = originalTextSentenceLengths;
    }

    @XmlElement(name = "TranslatedTextSentenceLengths")
    public TextSentenceLength getTranslatedTextSentenceLengths() {
        return translatedTextSentenceLengths;
    }

    public void setTranslatedTextSentenceLengths(
            TextSentenceLength translatedTextSentenceLengths) {
        this.translatedTextSentenceLengths = translatedTextSentenceLengths;
    }

    @XmlElement(name = "TranslatedText")
    public MSString getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(MSString translatedText) {
        this.translatedText = translatedText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MSTranslateArrayResponse)) return false;

        MSTranslateArrayResponse that = (MSTranslateArrayResponse) o;

        if (srcLanguage != null ? !srcLanguage.equals(that.srcLanguage) :
            that.srcLanguage != null) return false;
        if (alignment != null ? !alignment.equals(that.alignment) : that.alignment != null)
            return false;
        if (originalTextSentenceLengths != null ?
            !originalTextSentenceLengths
                .equals(that.originalTextSentenceLengths) :
            that.originalTextSentenceLengths != null) return false;
        if (translatedTextSentenceLengths != null ?
            !translatedTextSentenceLengths
                .equals(that.translatedTextSentenceLengths) :
            that.translatedTextSentenceLengths != null) return false;
        return translatedText != null ?
            translatedText.equals(that.translatedText) :
            that.translatedText == null;

    }

    @Override
    public int hashCode() {
        int result = srcLanguage != null ? srcLanguage.hashCode() : 0;
        result = 31 * result + (alignment != null ? alignment.hashCode() : 0);
        result = 31 * result + (originalTextSentenceLengths != null ?
            originalTextSentenceLengths.hashCode() : 0);
        result = 31 * result + (translatedTextSentenceLengths != null ?
            translatedTextSentenceLengths.hashCode() : 0);
        result =
            31 * result +
                (translatedText != null ? translatedText.hashCode() : 0);
        return result;
    }
}
