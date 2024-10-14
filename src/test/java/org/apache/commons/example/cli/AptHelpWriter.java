/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.apache.commons.example.cli;

import static java.lang.String.format;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.help.AbstractHelpWriter;
import org.apache.commons.cli.help.TableDefinition;
import org.apache.commons.cli.help.TextStyle;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;

/**
 * A class to write APT formatted text.
 */
public class AptHelpWriter extends AbstractHelpWriter {

    /**
     * Translator object for escaping APT codes
     */
    public static final CharSequenceTranslator ESCAPE_APT;

    static {
        final Map<CharSequence, CharSequence> escapeAptMap = new HashMap<>();
        escapeAptMap.put("\\", "\\\\");
        escapeAptMap.put("\"", "\\\"");
        escapeAptMap.put("*", "\\*");
        escapeAptMap.put("+", "\\+");
        escapeAptMap.put("|", "\\|");
        ESCAPE_APT = new LookupTranslator(escapeAptMap);
    }

    /**
     * Constructs a string of specified length filled with the specified char.
     *
     * @param len      the length of the final string.
     * @param fillChar the character to file it will.
     * @return A string of specified length filled with the specified char.
     * @since 1.10.0
     */
    static String filledString(final int len, final char fillChar) {
        final char[] padding = new char[len];
        Arrays.fill(padding, fillChar);
        return new String(padding);
    }

    public AptHelpWriter(final Appendable output) {
        super(output);
    }

    @Override
    public void appendHeader(final int level, final CharSequence text) throws IOException {
        if (StringUtils.isNotEmpty(text)) {
            if (level < 1) {
                throw new IllegalArgumentException("level must be at least 1");
            }
            for (int i = 0; i < level; i++) {
                output.append("*");
            }
            output.append(format(" %s%n%n", ESCAPE_APT.translate(text)));
        }
    }

    @Override
    public void appendList(final boolean ordered, final Collection<CharSequence> list) throws IOException {
        if (null != list) {
            if (ordered) {
                int idx = 1;
                for (final CharSequence s : list) {
                    output.append(format("    [[%s]] %s%n", idx++, ESCAPE_APT.translate(s)));
                }
            } else {
                for (final CharSequence s : list) {
                    output.append(format("    * %s%n", ESCAPE_APT.translate(s)));
                }
            }
            output.append(System.lineSeparator());
        }
    }

    @Override
    public void appendParagraph(final CharSequence paragraph) throws IOException {
        if (StringUtils.isNotEmpty(paragraph)) {
            output.append(format("  %s%n%n", ESCAPE_APT.translate(paragraph)));
        }
    }

    @Override
    public void appendTable(final TableDefinition table) throws IOException {
        if (table != null) {
            // create the row separator string
            final StringBuilder sb = new StringBuilder("*");
            for (int i = 0; i < table.headers().size(); i++) {
                final String header = table.headers().get(i);
                final TextStyle style = table.columnStyle().get(i);
                sb.append(filledString(header.length() + 2, '-'));
                switch (style.getAlignment()) {
                case LEFT:
                    sb.append("+");
                    break;
                case CENTER:
                    sb.append("*");
                    break;
                case RIGHT:
                    sb.append(":");
                    break;
                }
            }
            final String rowSeparator = System.lineSeparator() + sb.append(System.lineSeparator());

            // output the header line.
            output.append(sb.toString());
            output.append("|");
            for (final String header : table.headers()) {
                output.append(format(" %s |", ESCAPE_APT.translate(header)));
            }
            output.append(rowSeparator);

            // write the table entries
            for (final Collection<String> row : table.rows()) {
                output.append("|");
                for (final String cell : row) {
                    output.append(format(" %s |", ESCAPE_APT.translate(cell)));
                }
                output.append(rowSeparator);
            }

            // write the caption
            if (StringUtils.isNotEmpty(table.caption())) {
                output.append(format("%s%n", ESCAPE_APT.translate(table.caption())));
            }

            output.append(System.lineSeparator());
        }
    }

    @Override
    public void appendTitle(final CharSequence title) throws IOException {
        if (StringUtils.isNotEmpty(title)) {
            output.append(format("        -----%n        %1$s%n        -----%n%n%1$s%n%n", title));
        }
    }
}