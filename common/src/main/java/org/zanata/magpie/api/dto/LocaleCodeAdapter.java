package org.zanata.magpie.api.dto;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocaleCodeAdapter extends XmlAdapter<String, LocaleCode> {
    public @Nullable LocaleCode unmarshal(@Nullable String s) throws Exception {
        if (s == null) {
            return null;
        }
        return new LocaleCode(s);
    }

    public @Nullable String marshal(@Nullable LocaleCode localeCode) throws Exception {
        if (localeCode == null) {
            return null;
        }
        return localeCode.toString();
    }
}
