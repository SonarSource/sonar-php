/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
package org.sonar.php.highlighter;

import com.google.common.base.Charsets;
import com.sonar.sslr.api.typed.ActionParser;
import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerBracketTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import static org.fest.assertions.Assertions.assertThat;

public class SourceFileOffsetsTest {

  private static final ActionParser<Tree> PARSER = PHPParserBuilder.createParser(PHPLexicalGrammar.ARRAY_INIALIZER, Charsets.UTF_8);

  @Test
  public void first_line() throws Exception {
    String string = "[ \"value1\", \"value2\"]";
    ArrayInitializerBracketTree tree = (ArrayInitializerBracketTree)PARSER.parse(string);
    SourceFileOffsets offsets = new SourceFileOffsets(string);

    SyntaxToken firstToken  = ((LiteralTree) tree.arrayPairs().get(0).value()).token();
    assertThat(offsets.startOffset(firstToken)).isEqualTo(2);
    assertThat(offsets.endOffset(firstToken)).isEqualTo(10);
  }

  @Test
  public void second_line() throws Exception {
    String string = "[\n1,\r\n2\n]";
    ArrayInitializerBracketTree tree = (ArrayInitializerBracketTree) PARSER.parse(string);
    SourceFileOffsets offsets = new SourceFileOffsets(string);

    SyntaxToken firstElement  = ((LiteralTree) tree.arrayPairs().get(0).value()).token();
    assertThat(offsets.startOffset(firstElement)).isEqualTo(2);
    assertThat(offsets.endOffset(firstElement)).isEqualTo(3);

    SyntaxToken secondElement  = ((LiteralTree) tree.arrayPairs().get(1).value()).token();
    assertThat(offsets.startOffset(secondElement)).isEqualTo(6);
    assertThat(offsets.endOffset(secondElement)).isEqualTo(7);
  }
}
