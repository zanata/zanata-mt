package org.zanata.mt.model;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.mt.model.type.BackendIdType;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
@TypeDefs({
    @TypeDef(name = "backendIdType", typeClass = BackendIdType.class)
})
public class TextFlowTarget extends ModelEntity {
    private static final long serialVersionUID = -64231181018123191L;

    @NaturalId
    @ManyToOne(optional = false)
    @JoinColumn(name = "textFlowId", nullable = false, updatable = false)
    private TextFlow textFlow;

    @NaturalId
    @ManyToOne(optional = false)
    @JoinColumn(name = "localeId", nullable = false, updatable = false)
    private Locale locale;

    @NotEmpty
    private String content;

    @NotEmpty
    private String rawContent;

    private int usedCount;

    @Type(type = "backendIdType")
    @Column(nullable = false)
    private BackendID backendId;

    public TextFlowTarget() {
    }

    public TextFlowTarget(String content, String rawContent, TextFlow textFlow,
            Locale locale, BackendID backendID) {
        this.content = content;
        this.rawContent = rawContent;
        this.textFlow = textFlow;
        this.locale = locale;
        this.backendId = backendID;
    }

    public void incrementCount() {
        this.usedCount += 1;
    }

    public TextFlow getTextFlow() {
        return textFlow;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getContent() {
        return content;
    }

    public String getRawContent() {
        return rawContent;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public BackendID getBackendId() {
        return backendId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextFlowTarget)) return false;

        TextFlowTarget that = (TextFlowTarget) o;

        if (textFlow != null ? !textFlow.equals(that.textFlow) :
                that.textFlow != null) return false;
        return locale != null ? locale.equals(that.locale) :
                that.locale == null;
    }

    @Override
    public int hashCode() {
        int result = textFlow != null ? textFlow.hashCode() : 0;
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        return result;
    }
}
