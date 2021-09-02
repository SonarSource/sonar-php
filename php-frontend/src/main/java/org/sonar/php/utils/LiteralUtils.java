/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LiteralUtils {

  private LiteralUtils() {
    // This class only contains static methods
  }

  public static long longLiteralValue(String literalValue) {
    String value = literalValue.replace("_", "");
    if (value.startsWith("0b") || value.startsWith("0B")) {
      return Long.parseUnsignedLong(value.substring(2), 2);
    }
    return Long.decode(value);
  }

  // https://www.php.net/manual/en/language.types.string.php
  public static String stringLiteralValue(String literalValue) {
    return StringLiteralParser.stringValue(literalValue);
  }

  private abstract static class StringLiteralParser {

    private final String valueWithoutDelimiters;

    static String stringValue(String literalValue) {
      String valueWithoutDelimiters = literalValue.substring(1, literalValue.length() - 1);
      if (valueWithoutDelimiters.indexOf('\\') == -1) {
        return valueWithoutDelimiters;
      }
      StringLiteralParser parser = (literalValue.charAt(0) == '\'')
        ? new SingleQuotedStringLiteralParser(valueWithoutDelimiters)
        : new DoubleQuotedStringLiteralParser(valueWithoutDelimiters);
      return parser.stringValue();
    }

    StringLiteralParser(String valueWithoutDelimiters) {
      this.valueWithoutDelimiters = valueWithoutDelimiters;
    }

    String stringValue() {
      StringBuilder stringValue = new StringBuilder();
      boolean isInEscapeSequence = false;
      for (int i = 0; i < valueWithoutDelimiters.length(); i++) {
        char c = valueWithoutDelimiters.charAt(i);
        if (isInEscapeSequence) {
          String remainder = valueWithoutDelimiters.substring(i);
          int escapeSequenceLength = handleEscapeSequence(stringValue, c, remainder);
          i += escapeSequenceLength - 2;
          isInEscapeSequence = false;
        } else {
          if (c == '\\') {
            isInEscapeSequence = true;
          } else {
            stringValue.append(c);
          }
        }
      }
      return stringValue.toString();
    }

    /**
     * @return the total number of characters in the handled escape sequence
     */
    abstract int handleEscapeSequence(StringBuilder stringValue, char charAfterBackslash, String remainder);
  }

  private static class SingleQuotedStringLiteralParser extends StringLiteralParser {

    SingleQuotedStringLiteralParser(String valueWithoutDelimiters) {
      super(valueWithoutDelimiters);
    }

    @Override
    int handleEscapeSequence(StringBuilder stringValue, char charAfterBackslash, String remainder) {
      if (charAfterBackslash == '\'') {
        stringValue.append('\'');
      } else if (charAfterBackslash == '\\') {
        stringValue.append('\\');
      } else {
        stringValue.append('\\').append(charAfterBackslash);
      }
      return 2;
    }
  }

  private static class DoubleQuotedStringLiteralParser extends StringLiteralParser {

    DoubleQuotedStringLiteralParser(String valueWithoutDelimiters) {
      super(valueWithoutDelimiters);
    }

    @Override
    int handleEscapeSequence(StringBuilder stringValue, char charAfterBackslash, String remainder) {
      switch (charAfterBackslash) {
        case '\\':
          stringValue.append("\\");
          break;
        case '"':
          stringValue.append("\"");
          break;
        case 'n':
          stringValue.append("\n");
          break;
        case 'r':
          stringValue.append("\r");
          break;
        case 't':
          stringValue.append("\t");
          break;
        case 'f':
          stringValue.append("\f");
          break;
        case 'v':
          stringValue.append("\u000b");
          break;
        case 'e':
          stringValue.append("\u001b");
          break;
        case 'x':
          Matcher matcher = Pattern.compile("^x([0-9A-Fa-f]{1,2})").matcher(remainder);
          if (matcher.find()) {
            String hexValue = matcher.group(1);
            stringValue.append((char) Integer.parseInt(hexValue, 16));
            return hexValue.length() + 2;
          } else {
            stringValue.append("\\x");
          }
          break;
        default:
          stringValue.append('\\').append(charAfterBackslash);
      }
      return 2;
    }
  }

}
