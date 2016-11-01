package org.zanata.mt.model;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.mt.model.type.ProviderType;

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
    @TypeDef(name = "providerType", typeClass = ProviderType.class)
})
public class TextFlowTarget extends ModelEntity {
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

    private int usedCount;

    @Type(type = "providerType")
    @Column(nullable = false)
    private Provider provider;

    public TextFlowTarget() {
    }

    public TextFlowTarget(String content, TextFlow textFlow, Locale locale,
            Provider provider) {
        this.content = content;
        this.textFlow = textFlow;
        this.locale = locale;
        this.provider = provider;
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

    public int getUsedCount() {
        return usedCount;
    }

    public Provider getProvider() {
        return provider;
    }
}
