/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
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

import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.impl.channel.BlackHoleChannel;
import com.sonar.sslr.impl.channel.IdentifierAndKeywordChannel;
import com.sonar.sslr.impl.channel.PunctuatorChannel;
import org.sonar.php.PHPConfiguration;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.api.PHPTokenType;

import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.commentRegexp;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.regexp;

public class PHPLexer {

  /**
   * LF, CR, LS, PS
   */
  public static final String LINE_TERMINATOR = "\\n\\r\\u2028\\u2029";

  /**
   * Tab, Vertical Tab, Form Feed, Space, No-break space, Byte Order Mark, Any other Unicode "space separator"
   */
  public static final String WHITESPACE = "\\t\\u000B\\f\\u0020\\u00A0\\uFEFF\\p{Zs}";

  // HEREDOC
  public static final String HEREDOC = "(?s)"
    + "<<<[" + WHITESPACE + "]*\"([^\r\n'\"]++)\".*?(?:\\r\\n?+|\\n)\\1"
    + "|<<<[" + WHITESPACE + "]*'([^\r\n'\"]++)'.*?(?:\\r\\n?+|\\n)\\2"
    + "|<<<[" + WHITESPACE + "]*([^\r\n'\"]++).*?(?:\\r\\n?+|\\n)\\3";

  // IDENTIFIERS
  private static final String IDENTIFIER_START = "[a-zA-Z_\\x7f-\\xff]";
  public static final String IDENTIFIER_PART = "[" + IDENTIFIER_START + "[0-9]]";
  public static final String IDENTIFIER = IDENTIFIER_START + IDENTIFIER_PART + "*";

  private static final String VAR_IDENTIFIER_START = "\\$";
  public static final String VAR_IDENTIFIER = VAR_IDENTIFIER_START + IDENTIFIER;

  // COMMENTS

  /**
   * The "one-line" comment styles only comment to the end of the line or the current block of PHP code, whichever comes first.
   */
  private static final String SINGLE_LINE_COMMENT_CONTENT = "(?:(?!" + PHPTagsChannel.CLOSING + ")[^\\n\\r])*+";
  private static final String SINGLE_LINE_COMMENT1 = "//" + SINGLE_LINE_COMMENT_CONTENT;
  private static final String SINGLE_LINE_COMMENT2 = "#" + SINGLE_LINE_COMMENT_CONTENT;
  private static final String MULTI_LINE_COMMENT = "/\\*[\\s\\S]*?\\*/";
  public static final String COMMENT = "(?:" + SINGLE_LINE_COMMENT1 + "|" + SINGLE_LINE_COMMENT2 + "|" + MULTI_LINE_COMMENT + ")";

  // LITERALS

  private static final String EXECUTION_OPERATOR = "`[^`]*+`";

  // String
  public static final String STRING_LITERAL = "(?:"
    + "\"([^\"\\\\]*+(\\\\[\\s\\S])?+)*+\""
    + "|'([^'\\\\]*+(\\\\[\\s\\S])?+)*+'"
    + "|" + EXECUTION_OPERATOR
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
  public static final String NUMERIC_LITERAL = EXPONENT_DNUM + "|" + DNUM + "|" + INTEGER_LITERAL;

  private PHPLexer() {
  }

  public static Lexer create(PHPConfiguration conf) {
    Lexer.Builder builder = Lexer.builder()
      .withFailIfNoChannelToConsumeOneCharacter(true)
      .withCharset(conf.getCharset())

      .withChannel(new PHPTagsChannel())
//      .withChannel(new BlackHoleChannel("[" + WHITESPACE + LINE_TERMINATOR + "]++"))
//      .withChannel(commentRegexp(COMMENT))
//      .withChannel(regexp(PHPTokenType.HEREDOC, HEREDOC))

        // String Literals
//      .withChannel(regexp(PHPTokenType.NUMERIC_LITERAL, NUMERIC_LITERAL))
//      .withChannel(regexp(PHPTokenType.STRING_LITERAL, STRING_LITERAL))

//      .withChannel(new IdentifierAndKeywordChannel(IDENTIFIER, false, PHPKeyword.values()))
//      .withChannel(regexp(PHPTokenType.VAR_IDENTIFIER, VAR_IDENTIFIER))

//      .withChannel(new PunctuatorChannel(PHPPunctuator.values()))
 ;

    return builder.build();
  }

}
