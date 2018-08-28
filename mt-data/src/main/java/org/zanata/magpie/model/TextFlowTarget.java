package org.zanata.magpie.model;

import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
@NamedQueries({
        @NamedQuery(name = TextFlowTarget.QUERY_FIND_BY_LOCALE_BACKEND,
                query = "from TextFlowTarget where textFlow = :textFlow and locale = :locale and backendId = :backendId")
})
public class TextFlowTarget extends ModelEntity {
    private static final long serialVersionUID = -64231181018123191L;
    public static final String QUERY_FIND_BY_LOCALE_BACKEND = "findByLocaleAndBackEnd";

    @NaturalId
    @ManyToOne(optional = false)
    @JoinColumn(name = "textFlowId", nullable = false, updatable = false)
    @NotNull
    private TextFlow textFlow;

    @NaturalId
    @ManyToOne(optional = false)
    @JoinColumn(name = "localeId", nullable = false, updatable = false)
    @NotNull
    private Locale locale;

    @NotEmpty
    private String content;

    @NotEmpty
    private String rawContent;

    @NaturalId
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private BackendID backendId;

    public TextFlowTarget() {
    }

    public TextFlowTarget(String content, String rawContent, TextFlow textFlow,
            Locale locale, BackendID backendID) {
        this.content = content;
        this.rawContent = rawContent;
        this.locale = locale;
        this.backendId = backendID;
        this.textFlow = textFlow;
    }

    public void updateContent(String content, String rawContent) {
        this.content = content;
        this.rawContent = rawContent;
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

    public BackendID getBackendId() {
        return backendId;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof TextFlowTarget)) return false;

        TextFlowTarget that = (TextFlowTarget) o;

        if (getTextFlow() != null ? !getTextFlow().equals(that.getTextFlow()) :
                that.getTextFlow() != null) return false;
        if (getLocale() != null ? !getLocale().equals(that.getLocale()) :
                that.getLocale() != null) return false;
        return getBackendId() != null ?
                getBackendId().equals(that.getBackendId()) :
                that.getBackendId() == null;
    }

    @Override
    public int hashCode() {
        int result = getTextFlow() != null ? getTextFlow().hashCode() : 0;
        result = 31 * result +
                (getLocale() != null ? getLocale().hashCode() : 0);
        result =
                31 * result +
                        (getBackendId() != null ? getBackendId().hashCode() :
                                0);
        return result;
    }

    @Override
    public String toString() {
        return "TextFlowTarget{" +
                "textFlow=" + textFlow +
                ", locale=" + locale +
                ", content='" + content + '\'' +
                ", rawContent='" + rawContent + '\'' +
                ", backendId=" + backendId +
                '}';
    }
}
