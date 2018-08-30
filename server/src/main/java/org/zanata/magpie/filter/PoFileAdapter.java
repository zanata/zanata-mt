/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.magpie.filter;

import org.apache.commons.lang3.tuple.Pair;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.PoWriter;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.api.dto.DocumentContent;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.api.dto.TypeString;
import org.zanata.magpie.util.HashUtil;

import javax.ws.rs.core.MediaType;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class PoFileAdapter implements TranslationFileAdapter {

    private static final Logger LOG =
        LoggerFactory.getLogger(PoFileAdapter.class);

    private static final String TRANSLATION_FILE_EXTENSION = "po";

    private final Charset charset;

    public PoFileAdapter(Charset charset) {
        this.charset = charset;
    }

    @Override
    public Pair<DocumentContent, Map<String, Message>> parseSourceDocument(
        InputStream inputStream, String url, LocaleCode fromLocaleCode) {
        List<TypeString> contents = new ArrayList<>();
        Map<String, Message> messages = new LinkedHashMap<>();

        MessageStreamParser messageParser = createParser(inputStream);
        while (messageParser.hasNext()) {
            Message message = messageParser.next();
            String id = createId(message);
            if (message.isHeader()) {
                // log.warn("term: [{}] is ignored - message is header",
                // message.getMsgid());
            } else if (message.isObsolete()) {
                // log.warn("term: [{}] is ignored - message obsolete",
                // message.getMsgid());
            } else {
                contents.add(new TypeString(getSourceContent(message),
                    MediaType.TEXT_PLAIN_TYPE.toString(), id));
            }
            messages.put(id, message);

        }
        return Pair.of(
            new DocumentContent(contents, url, fromLocaleCode.getId()), messages);

    }

    /**
     * Requires #parseSourceDocument to be triggered first. This is meant to be a
     * single process from parseSourceDocument to writeTranslatedFile.
     *
     * @param output
     * @param fromLocaleCode
     * @param toLocaleCode
     * @param translatedDocContent
     * @throws IOException
     */
    @Override
    public void writeTranslatedFile(@NotNull OutputStream output,
        LocaleCode fromLocaleCode, LocaleCode toLocaleCode,
        DocumentContent translatedDocContent, Map<String, Message> messages,
        String attribution) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(output, charset));
        PoWriter poWriter = new PoWriter();

        if (messages == null || messages.isEmpty()) {
            LOG.warn(
                "No messages to output. Please make sure parseSourceDocument is triggered before output file");
            return;
        }
        for (TypeString trans : translatedDocContent.getContents()) {
            Message message = messages.get(trans.getMetadata());
            message.setMsgstr(trans.getValue());
        }
        boolean haveInsertedAttribution = false;
        for (Map.Entry<String, Message> entry : messages.entrySet()) {
            if (entry.getValue().isHeader()) {
                if (!haveInsertedAttribution) {
                    entry.getValue().addComment(attribution);
                    haveInsertedAttribution = true;
                }
                poWriter.write(entry.getValue(), writer);
                writer.write("\n");
            } else {
                if (!haveInsertedAttribution) {
                    // if we haven't written a header yet, generate one for the attribution
                    Message message = new Message();
                    message.addComment(attribution);
                    poWriter.write(message, writer);
                    writer.write("\n");
                    haveInsertedAttribution = true;
                }
                poWriter.write(entry.getValue(), writer);
                writer.write("\n");
            }
        }
        messages.clear();
    }

    @Override
    public String getTranslationFileExtension() {
        return TRANSLATION_FILE_EXTENSION;
    }

    // if plural, returns the last string in the list
    private String getSourceContent(Message message) {
        return message.isPlural() ? message.getMsgidPlural() :
            message.getMsgid();
    }

    static MessageStreamParser createParser(InputStream inputStream) {
        return new MessageStreamParser(inputStream, StandardCharsets.UTF_8);
    }

    static String createId(Message message) {
        String sep = "\u0000";
        String hashBase =
            message.getMsgctxt() == null ? message.getMsgid() : message
                .getMsgctxt() + sep + message.getMsgid();
        return HashUtil.generateHash(hashBase);
    }
}
