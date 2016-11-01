package org.zanata.mt.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.util.TranslationUtil;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
public class TextFlow extends ModelEntity {
    @NotEmpty
    @Size(max = 255)
    private String hash;

    @NaturalId
    @ManyToOne(optional = false)
    @JoinColumn(name = "localeId", nullable = false, updatable = false)
    private Locale locale;

    @NotEmpty
    private String content;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "textFlow",
        fetch = FetchType.EAGER)
    private List<TextFlowTarget> targets = new ArrayList<>();

    public TextFlow() {
    }

    public TextFlow(String content, Locale locale) {
        this.content = content;
        this.locale = locale;
        updateContentHash();
    }

    public void setContent(String content) {
        this.content = content;
        updateContentHash();
    }

    @Transient
    private void updateContentHash() {
        this.setHash(
                TranslationUtil.generateHash(content, locale.getLocaleId()));
    }

    public String getHash() {
        return hash;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getContent() {
        return content;
    }

    public List<TextFlowTarget> getTargets() {
        return targets;
    }

    protected void setHash(String hash) {
        this.hash = hash;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Transient
    public List<TextFlowTarget> getTargetsByLocaleId(LocaleId localeId) {
        return getTargets().stream().filter(new Predicate<TextFlowTarget>() {
            @Override
            public boolean test(TextFlowTarget target) {
                return target.getLocale().getLocaleId().equals(localeId);
            }
        }).collect(Collectors.toList());
    }
}
