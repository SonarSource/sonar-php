/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.php.parser;

public class LexicalConstant {

  /**
   * PHP TAGS & INLINE HTML
   */
  public static final String PHP_OPENING_TAG = "(?i)(?:<\\?(?:php|=|)|<%)";
  // we use "[\s\S]" instead of "." as the dot doesn't consume line endings
  private static final String ANY_CHAR_BUT_START_TAG = "((?!" + PHP_OPENING_TAG + ")[\\s\\S])";
  public static final String PHP_CLOSING_TAG = "(?:[\\?%]>)";
  public static final String PHP_START_TAG = ANY_CHAR_BUT_START_TAG + "*+(" + PHP_OPENING_TAG + ")";
  public static final String PHP_END_TAG = PHP_CLOSING_TAG + PHP_START_TAG + "?+";
  public static final String ANYTHING_BUT_START_TAG = ANY_CHAR_BUT_START_TAG + "++";

  /**
   * WHITESPACES
   */

  /**
   * LF, CR, LS, PS
   */
  public static final String LINE_TERMINATOR = "\\n\\r\\u2028\\u2029";

  /**
   * Tab, Vertical Tab, Form Feed, Space, No-break space, Byte Order Mark, Any other Unicode "space separator"
   */
  public static final String WHITESPACE = "\\t\\u000B\\f\\u0020\\u00A0\\uFEFF\\p{Zs}";

  /**
   * IDENTIFIERS
   */
  private static final String IDENTIFIER_START = "[a-zA-Z_\\x7f-\\xff]";
  public static final String IDENTIFIER_PART = "[" + IDENTIFIER_START + "[0-9]]";
  public static final String IDENTIFIER = IDENTIFIER_START + IDENTIFIER_PART + "*";

  private static final String VAR_IDENTIFIER_START = "\\$";
  public static final String VAR_IDENTIFIER = VAR_IDENTIFIER_START + IDENTIFIER;

  /**
   * COMMENT
   */

  /**
   * The "one-line" comment styles only comment to the end of the line or the current block of PHP code, whichever comes first.
   */
  private static final String SINGLE_LINE_COMMENT_CONTENT = "(?:(?!" + PHP_CLOSING_TAG + ")[^\\n\\r])*+";
  private static final String SINGLE_LINE_COMMENT1 = "//" + SINGLE_LINE_COMMENT_CONTENT;
  private static final String SINGLE_LINE_COMMENT2 = "#" + SINGLE_LINE_COMMENT_CONTENT;
  private static final String MULTI_LINE_COMMENT = "/\\*[\\s\\S]*?\\*/";
  public static final String COMMENT = "(?:" + SINGLE_LINE_COMMENT1 + "|" + SINGLE_LINE_COMMENT2 + "|" + MULTI_LINE_COMMENT + ")";

  /**
   * LITERAL
   */

  /**
   * '$' sign is allowed in double quoted string and heredoc only when it does not conflict with the
   * encapsulated variable expression, i.e when it not followed with '{' or a starting identifier character.
   */
  private static final String PERMITTED_EMBEDDED_DOLAR = "(?:\\$(?!\\{|" + IDENTIFIER_START + "))";
  private static final String PERMITTED_OPEN_CURLY_BRACE = "(?:\\{(?!\\$))";
  private static final String NON_SPECIAL_CHARACTERS = "(?:[^\"\\\\$\\{])";
  private static final String NON_SPECIAL_CHARACTERS_HEREDOC = "(?:[^\\\\$\\{])";
  private static final String ESCAPED_CHARACTERS = "(?:\\\\[\\s\\S])";

  public static final String STRING_WITH_ENCAPS_VAR_CHARACTERS = "(?:(?:"
    + NON_SPECIAL_CHARACTERS
    + "|" + PERMITTED_EMBEDDED_DOLAR
    + "|" + PERMITTED_OPEN_CURLY_BRACE
    + "|" + ESCAPED_CHARACTERS
    + ")++)";

  public static final String HEREDOC_STRING_CHARACTERS = "(?:(?:"
    + NON_SPECIAL_CHARACTERS_HEREDOC
    + "|" + PERMITTED_EMBEDDED_DOLAR
    + "|" + PERMITTED_OPEN_CURLY_BRACE
    + "|" + ESCAPED_CHARACTERS
    + ")++)";

  private static final String EXECUTION_OPERATOR = "`[^`]*+`";

  /**
   * Heredoc / Nowdoc
   */
  public static final String NEW_LINE = "(?:\r\n?+|\n)";
  public static final String HEREDOC = "(?s)(<<<[ \t\u000B\f]*\"?([^\r\n'\"]++)\"?" + NEW_LINE + ")(?:(.*?)" + NEW_LINE + ")?\\2";
  public static final String NOWDOC = "(?s)<<<[ \t\u000B\f]*'([^\r\n'\"]++)'" + NEW_LINE + "(?:.*?" + NEW_LINE + ")?\\1";

  /**
   * String
   */
  public static final String STRING_LITERAL = "(?:"
    + "\"" + STRING_WITH_ENCAPS_VAR_CHARACTERS + "?+" + "\""
    + "|'([^'\\\\]*+(\\\\[\\s\\S])?+)*+'"
    + "|" + EXECUTION_OPERATOR
    + ")";

  /**
   * Integer
   */
  private static final String DECIMAL = "[1-9][0-9]*+|0";
  private static final String HEXADECIMAL = "0[xX][0-9a-fA-F]++";
  private static final String OCTAL = "0[0-7]++";
  private static final String BINARY = "0b[01]++";
  private static final String INTEGER_LITERAL = OCTAL
    + "|" + HEXADECIMAL
    + "|" + BINARY
    + "|" + DECIMAL;

  /**
   * Floating point
   */
  private static final String LNUM = "[0-9]+";
  private static final String DNUM = "([0-9]*[\\.]" + LNUM + ")"
    + "|(" + LNUM + "[\\.][0-9]*)";
  private static final String EXPONENT_DNUM = "((" + LNUM + "|" + DNUM + ")[eE][+-]?" + LNUM + ")";

  /**
   * Numeric Literal
   */
  public static final String NUMERIC_LITERAL = EXPONENT_DNUM + "|" + DNUM + "|" + INTEGER_LITERAL;

  private LexicalConstant() {
  }

}
