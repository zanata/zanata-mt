/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.magpie.util;

import static java.text.BreakIterator.getCharacterInstance;
import static java.util.Locale.forLanguageTag;

import java.text.BreakIterator;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.steps.tokenization.Tokenizer;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

public class CountUtil {
    private static final Logger log = LoggerFactory.getLogger(CountUtil.class);

    private CountUtil() {
    }

    /**
     * Count words using Okapi's WordCounter, which tries to implement the LISA
     * standard <a href=
     * "http://web.archive.org/web/20090403134742/http://www.lisa.org/Global-information-m.105.0.html"
     * >GMX-V</a>
     *
     * @param content
     * @param localeCode
     */
    public static long countWords(@NotNull String content,
            @NotNull String localeCode) {
        if (StringUtils.isBlank(content) || StringUtils.isBlank(localeCode)) {
            return 0;
        }
        try {
            LocaleId locale = getLocaleId(localeCode);

            Tokens tokens = StringTokenizer.tokenizeString(content, locale, "WORD");
            return tokens.size();
        } catch (Exception e) {
            Object[] args = new Object[] { content, localeCode, e };
            log.error("unable to count words in string '{}' for locale '{}'",
                    args);
            return 0;
        }
    }

    private static LocaleId getLocaleId(@NotNull String localeCode) {
        LocaleId locale;
        try {
            locale = LocaleId.fromBCP47(localeCode);
        } catch (Exception e) {
            log.error(
                    "can't understand '{}' as a BCP-47 locale; defaulting to English",
                    localeCode);
            locale = LocaleId.ENGLISH;
        }
        return locale;
    }

    private static class StringTokenizer extends Tokenizer {
        public static Tokens tokenizeString(String text, LocaleId language,
                String... tokenNames) {
            synchronized (Tokenizer.class) {
                return Tokenizer.tokenizeString(text, language, tokenNames);
            }
        }
    }

    /**
     * Count the characters in a string by code point.
     * See https://stackoverflow.com/a/6846155/345718
     *
     * @param content
     *            the string content
     * @return character count
     */
    public static long countCharacters(@Nonnull String content) {
        if (StringUtils.isBlank(content)) {
            return 0;
        }
        return content.codePointCount(0, content.length());
    }

}
