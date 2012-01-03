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
package org.sonar.plugins.php.duplications;

import org.sonar.plugins.php.duplications.internal.TokenChunker;

/**
 * No PHP language specification document could be found to help produce this tokenizer. So http://php.net/manual/en/langref.php was used
 * for that purpose. <br/>
 * <br/>
 * <i>Note: Heredoc (http://www.php.net/manual/en/language.types.string.php#language.types.string.syntax.heredoc) and Nowdoc
 * (http://www.php.net/manual/en/language.types.string.php#language.types.string.syntax.nowdoc) are not supported yet. Writing a
 * pre-processor would be required to handle those cases. </i>
 */
public final class PhpTokenProducer {

  private PhpTokenProducer() {
  }

  private static final String NORMALIZED_CHARACTER_LITERAL = "$CHARS";
  private static final String NORMALIZED_NUMERIC_LITERAL = "$NUMBER";

  private static final String EXP = "([Ee][+-]?+[0-9]++)";

  /**
   * Creates a {@link TokenChunker} for the PHP language.
   * 
   * @return a TokenChunker
   */
  public static TokenChunker build() {
    return TokenChunker.builder()
        // White Space
        .ignore("\\s")
        // Comments
        .ignore("//[^\\n\\r]*+")
        .ignore("#[^\\n\\r]*+")
        .ignore("/\\*[\\s\\S]*?\\*/")
        // String Literals
        .token("\"([^\"\\\\]*+(\\\\[\\s\\S])?+)*+\"", NORMALIZED_CHARACTER_LITERAL)
        // Character Literals
        .token("'([^'\\n\\\\]*+(\\\\.)?+)*+'", NORMALIZED_CHARACTER_LITERAL)
        // Identifiers, Keywords, Boolean Literals, The Null Literal
        .token("[a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*+")
        // Floating-Point Literals
        .token("[+-]?[0-9]++\\.([0-9]++)?+" + EXP + "?+", NORMALIZED_NUMERIC_LITERAL) // Decimal
        .token("[+-]?\\.[0-9]++" + EXP + "?+", NORMALIZED_NUMERIC_LITERAL) // Decimal
        .token("[+-]?[0-9]++" + EXP, NORMALIZED_NUMERIC_LITERAL) // Decimal
        // Integer Literals
        .token("[+-]?0[xX][0-9a-fA-F]++", NORMALIZED_NUMERIC_LITERAL) // Hexadecimal
        .token("[+-]?0[b][01]++", NORMALIZED_NUMERIC_LITERAL) // Binary
        .token("[+-]?[0-9]++", NORMALIZED_NUMERIC_LITERAL) // Decimal and Octal
        // Any other character
        .token(".")
        .build();
  }

}
