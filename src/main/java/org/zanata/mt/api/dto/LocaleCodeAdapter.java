package org.zanata.mt.api.dto;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocaleCodeAdapter extends XmlAdapter<String, LocaleCode> {
    public LocaleCode unmarshal(String s) throws Exception {
        if (s == null) {
            return null;
        }
        return new LocaleCode(s);
    }

    public String marshal(LocaleCode localeCode) throws Exception {
        if (localeCode == null) {
            return null;
        }
        return localeCode.toString();
    }
}
