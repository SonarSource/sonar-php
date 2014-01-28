/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.lexer;

import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.impl.channel.BlackHoleChannel;
import com.sonar.sslr.impl.channel.IdentifierAndKeywordChannel;
import com.sonar.sslr.impl.channel.PunctuatorChannel;

import java.nio.charset.Charset;

import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.commentRegexp;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.regexp;

public class PHPLexer {

  private static final String WHITESPACE = "[ \\t\\r\\n]++";

  // PHP Tags
  private static final String OPEN_TAG = "<\\?php|<\\?|<%";
  private static final String CLOSE_TAG = "\\?>|%>";

  // IDENTIFIERS
  private static final String LABEL = "[a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*";
  private static final String VAR_IDENTIFIER_START = "\\$";
  private static final String VAR_IDENTIFIER = VAR_IDENTIFIER_START + LABEL;

  // COMMENTS
  public static final String SINGLE_LINE_COMMENT1 = "//[^\\n\\r]*+";
  public static final String SINGLE_LINE_COMMENT2 = "#[^\\n\\r]*+";
  public static final String MULTI_LINE_COMMENT = "/\\*[\\s\\S]*?\\*/";
  public static final String COMMENT = "(?:" + SINGLE_LINE_COMMENT1 + "|" + SINGLE_LINE_COMMENT2 + "|" + MULTI_LINE_COMMENT + ")";

  // LITERALS

  // String
  public static final String STRING_LITERAL = "(?:"
    + "\"([^\"\\\\]*+(\\\\[\\s\\S])?+)*+\""
    + "|'([^'\\\\]*+(\\\\[\\s\\S])?+)*+'"
    + ")";

  // Integer
  private static final String DECIMAL = "[1-9][0-9]*+|0";
  private static final String HEXADECIMAL = "0[xX][0-9a-fA-F]++";
  private static final String OCTAL = "0[0-7]++";
  private static final String BINARY = "0b[01]++";
  private static final String INTEGER_LITERAL = OCTAL
    + "|" + HEXADECIMAL
    + "|" + BINARY
    + "|" + DECIMAL;

  // Floating point
  private static final String LNUM = "[0-9]+";
  private static final String DNUM = "([0-9]*[\\.]" + LNUM + ")"
    + "|(" + LNUM + "[\\.][0-9]*)";
  private static final String EXPONENT_DNUM = "((" + LNUM + "|" + DNUM + ")[eE][+-]?" + LNUM + ")";

  // Numeric
  private static final String NUMERIC_LITERAL = EXPONENT_DNUM + "|" + DNUM + "|" + INTEGER_LITERAL;

  public static Lexer create(Charset charset) {
    Lexer.Builder builder = Lexer.builder()
      .withFailIfNoChannelToConsumeOneCharacter(true)
      .withCharset(charset)

      .withChannel(new BlackHoleChannel(WHITESPACE))
      .withChannel(commentRegexp(COMMENT))

      // String Literals
      .withChannel(regexp(GenericTokenType.LITERAL, NUMERIC_LITERAL))
      .withChannel(regexp(GenericTokenType.LITERAL, STRING_LITERAL))

      // PHP tags
      .withChannel(regexp(PHPTokenType.OPEN_TAG, OPEN_TAG))
      .withChannel(regexp(PHPTokenType.CLOSE_TAG, CLOSE_TAG))

      .withChannel(new IdentifierAndKeywordChannel(LABEL, false, PHPKeyword.values()))
      .withChannel(regexp(PHPTokenType.VAR_IDENTIFIER, VAR_IDENTIFIER))

      .withChannel(new PunctuatorChannel(PHPPunctuator.values()));

    return builder.build();
  }

}
