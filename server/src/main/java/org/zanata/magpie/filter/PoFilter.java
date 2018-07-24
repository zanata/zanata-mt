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

import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class PoFilter implements Filter {

    private static final Logger LOG =
        LoggerFactory.getLogger(PoFilter.class);

    private static final String TRANSLATION_FILE_EXTENSION = "po";
    private final static String HEADER = "X-Generator: Magpie Machine Translations";

    private Map<String, Message> messages;
    private final Charset charset;

    public PoFilter(Charset charset) {
        this.charset = charset;
        this.messages = new LinkedHashMap<>();
    }

    @Override
    public DocumentContent parseDocument(InputStream inputStream,
        String fileName, LocaleCode fromLocaleCode) {
        InputSource potInputSource = new InputSource(inputStream);
        List<TypeString> contents = new ArrayList<>();
        MessageStreamParser messageParser = createParser(potInputSource);
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
        return new DocumentContent(contents, fileName, fromLocaleCode.getId());

    }

    /**
     * Requires #parseDocument to be triggered first. This is meant to be a
     * single process from parseDocument to writeTranslatedFile.
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
        DocumentContent translatedDocContent) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(output, charset));

        if (messages == null || messages.isEmpty()) {
            LOG.warn(
                "No messages to output. Please make sure parseDocument is triggered before output file");
            return;
        }

        for (TypeString trans : translatedDocContent.getContents()) {
            Message message = messages.get(trans.getMetadata());
            message.setMsgstr(trans.getValue());
        }
        boolean insertHeader = false;
        for (Map.Entry<String, Message> entry : messages.entrySet()) {
            if (entry.getValue().isHeader()) {
                if (!insertHeader) {
                    entry.getValue().addComment(HEADER);
                    insertHeader = true;
                }
                write(entry.getValue(), writer);
            } else {
                if (!insertHeader) {
                    Message message = new Message();
                    message.addComment(HEADER);
                    write(message, writer);
                    insertHeader = true;
                }
                write(entry.getValue(), writer);
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
        if (message.isPlural()) {
            List<String> sourceContents =
                Arrays.asList(message.getMsgid(), message.getMsgidPlural());
            return sourceContents.get(sourceContents.size() - 1);
        }
        return message.getMsgid();
    }

    public void write(Message message, Writer writer) throws IOException {

        for (String comment : message.getComments()) {
            writeComment("# ", comment, writer);
        }

        for (String comment : message.getExtractedComments()) {
            writeComment("#. ", comment, writer);
        }

        for (String sourceRef : message.getSourceReferences()) {
            writeComment("#: ", sourceRef, writer);
        }

        Collection<String> formats = message.getFormats();
        if (!formats.isEmpty()) {
            writer.write("#");
            for (String format : formats) {
                writer.write(", ");
                writer.write(format);
            }
            writer.write('\n');
        }

        if (message.getPrevMsgctx() != null) {
            writeMsgctxt("#| ", message.getPrevMsgctx(), writer);
        }

        if (message.getPrevMsgid() != null) {
            writeMsgid("#| ", message.getPrevMsgid(), writer);
        }

        if (message.getPrevMsgidPlural() != null) {
            writeMsgidPlural("#| ", message.getPrevMsgidPlural(), writer);
        }

        String prefix = message.isObsolete() ? "#~ " : "";
        if (message.getMsgctxt() != null) {
            writeMsgctxt(prefix, message.getMsgctxt(), writer);
        }

        if (message.isPlural()) {
            writeMsgid(prefix, message.getMsgid(), writer);
            writeMsgidPlural(prefix, message.getMsgidPlural(), writer);
            writeMsgstrPlurals(prefix, message.getMsgstrPlural(), writer);
        } else {
            writeMsgid(prefix, message.getMsgid(), writer);
            writeMsgstr(prefix, message.getMsgstr(), writer);
        }
        writer.flush();
    }

    protected void writeComment(String prefix, String comment, Writer writer)
        throws IOException {
        String[] lines = comment.split("\n");
        for (String line : lines) {
            writer.write(prefix);
            writer.write(line);
            writer.write('\n');
        }
    }

    protected void writeMsgctxt(String prefix, String ctxt, Writer writer)
        throws IOException {
        String msgSpace = "msgctxt ";
        writer.write(prefix + msgSpace);
        writeString(prefix, ctxt, writer, msgSpace.length());
    }

    protected void writeMsgid(String prefix, String msgid, Writer writer)
        throws IOException {
        String msgSpace = "msgid ";
        writer.write(prefix + msgSpace);
        writeString(prefix, msgid, writer, msgSpace.length());
    }

    protected void writeMsgidPlural(String prefix, String msgidPlural,
        Writer writer) throws IOException {
        String msgSpace = "msgid_plural ";
        writer.write(prefix + msgSpace);
        writeString(prefix, msgidPlural, writer, msgSpace.length());
    }

    protected void writeMsgstr(String prefix, String msgstr, Writer writer)
        throws IOException {
        if (msgstr == null) {
            msgstr = "";
        }
        String msgSpace = "msgstr ";
        writer.write(prefix + msgSpace);
        writeString(prefix, msgstr, writer, msgSpace.length());
    }

    protected void writeMsgStrPlural(String prefix, String msgstr, int i,
        Writer writer) throws IOException {
        String msgSpace = "msgstr[" + i + "] ";
        writer.write(prefix + msgSpace);
        writeString(prefix, msgstr, writer, msgSpace.length());
    }

    protected void writeMsgstrPlurals(String prefix,
        List<String> msgstrPlurals, Writer writer) throws IOException {
        if (msgstrPlurals.isEmpty()) {
            writeMsgStrPlural(prefix, "", 0, writer);
        } else {
            int i = 0;
            for (String msgstr : msgstrPlurals) {
                writeMsgStrPlural(prefix, msgstr, i, writer);
                i++;
            }
        }
    }

    protected void writeString(String prefix, String s, Writer writer,
        int firstLineContextWidth) throws IOException {
        writeString(prefix, s, writer, firstLineContextWidth, 80, 0);
    }

    protected void writeString(String prefix, String s, Writer writer,
        int firstLineContextWidth, int colWidth, int indent)
        throws IOException {
        // This is for obsolete entry processing. When the first line
        // is not empty, it doesn't need to output "#~".
        boolean firstline = true;

        writer.write('\"');

        // check if we should output a empty first line
        int firstLineEnd = s.indexOf('\n');
        if ((firstLineEnd != -1 && firstLineEnd > (colWidth
                - firstLineContextWidth - 4)) || s.length() > (colWidth
                - firstLineContextWidth - 4)) {
            firstline = false;
            writer.write('\"');
            writer.write('\n');
            if (prefix.isEmpty())
                writer.write('\"');
        }

        StringBuilder currentLine = new StringBuilder(100);

        int lastSpacePos = 0;

        for (int i = 0; i < s.length(); i++) {
            char currentChar = s.charAt(i);

            switch (currentChar) {
                case '\n':
                    currentLine.append('\\');
                    currentLine.append('n');
                    if (i != s.length() - 1) {
                        firstline = writeNewline(prefix, firstline, writer,
                            currentLine);

                        if (prefix.isEmpty()) {
                            writer.write(currentLine.toString());
                            writer.write('\"');
                            writer.write('\n');
                            writer.write('\"');
                        }

                        lastSpacePos = 0;
                        currentLine.delete(0, currentLine.length());
                    }
                    break;
                case '\\':
                    currentLine.append(currentChar);
                    currentLine.append(currentChar);
                    break;
                case '\r':
                    currentLine.append('\\');
                    currentLine.append('r');
                    break;
                case '\t':
                    currentLine.append(currentChar);
                    break;
                case '"':
                    currentLine.append('\\');
                    currentLine.append(currentChar);
                    break;
                case ':':
                case '.':
                case '/':
                case '-':
                case '=':
                case ' ':
                    lastSpacePos = currentLine.length();
                    currentLine.append(currentChar);
                    break;
                default:
                    currentLine.append(currentChar);
            }

            if (currentLine.length() > colWidth - 4
                && lastSpacePos != 0) {
                if (!prefix.isEmpty() && !firstline) {
                    writer.write(prefix);
                    writer.write('\"');
                    writer.write(currentLine.substring(0, lastSpacePos + 1));
                    writer.write('\"');
                    writer.write('\n');
                }

                if (!prefix.isEmpty() && firstline) {
                    writer.write(currentLine.substring(0, lastSpacePos + 1));
                    writer.write('\"');
                    writer.write('\n');
                    firstline = false;
                }

                if (prefix.isEmpty()) {
                    writer.write(currentLine.substring(0, lastSpacePos + 1));
                    writer.write('\"');
                    writer.write('\n');
                    writer.write('\"');
                }
                currentLine.delete(0, lastSpacePos + 1);
                lastSpacePos = 0;
            }
        }
        firstline = writeNewline(prefix, firstline, writer, currentLine);

        if (prefix.isEmpty()) {
            writer.write(currentLine.toString());
            writer.write('\"');
            writer.write('\n');
        }
    }

    private boolean writeNewline(String prefix, boolean firstline,
        Writer writer, StringBuilder currentLine) throws IOException {
        boolean isFirstline = firstline;

        if (!prefix.isEmpty() && !firstline) {
            writer.write(prefix);
            writer.write('\"');
            writer.write(currentLine.toString());

            writer.write('\"');
            writer.write('\n');
        }

        if (!prefix.isEmpty() && firstline) {
            writer.write(currentLine.toString());
            writer.write('\"');
            writer.write('\n');
            isFirstline = false;
        }
        return isFirstline;
    }

    static MessageStreamParser createParser(InputSource inputSource) {
        MessageStreamParser messageParser;
        if (inputSource.getCharacterStream() != null)
            messageParser =
                new MessageStreamParser(inputSource.getCharacterStream());
        else if (inputSource.getByteStream() != null) {
            if (inputSource.getEncoding() != null)
                messageParser =
                    new MessageStreamParser(inputSource.getByteStream(),
                        Charset.forName(inputSource.getEncoding()));
            else
                messageParser =
                    new MessageStreamParser(inputSource.getByteStream(),
                        Charset.forName("UTF-8"));
        } else if (inputSource.getSystemId() != null) {
            try {
                URL url = new URL(inputSource.getSystemId());

                if (inputSource.getEncoding() != null)
                    messageParser =
                        new MessageStreamParser(url.openStream(),
                            Charset.forName(inputSource.getEncoding()));
                else
                    messageParser =
                        new MessageStreamParser(url.openStream(),
                            Charset.forName("UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException(
                    "failed to get input from url in inputSource", e);
            }
        } else {
            throw new RuntimeException("not a valid inputSource");
        }
        return messageParser;
    }

    static String createId(Message message) {
        String sep = "\u0000";
        String hashBase =
            message.getMsgctxt() == null ? message.getMsgid() : message
                .getMsgctxt() + sep + message.getMsgid();
        return HashUtil.generateHash(hashBase);
    }
}
