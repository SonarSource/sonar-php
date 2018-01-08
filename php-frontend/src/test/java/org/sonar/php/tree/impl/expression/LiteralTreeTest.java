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
package org.sonar.php.tree.impl.expression;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.assertj.core.api.Assertions.assertThat;

public class LiteralTreeTest extends PHPTreeModelTest {

  @Test
  public void null_literal() throws Exception {
    assertLiteral("NULL", Kind.NULL_LITERAL);
  }

  @Test
  public void nowdoc_literal() throws Exception {
    assertLiteral("<<<'EOD'\n content \nEOD", Kind.NOWDOC_LITERAL);
    assertLiteral("<<< 'EOD'\n content \nEOD", Kind.NOWDOC_LITERAL);
    assertLiteral("<<<'EOD'\nEOD", Kind.NOWDOC_LITERAL);
  }

  @Test
  public void numeric_literal() throws Exception {
    assertLiteral("0", Kind.NUMERIC_LITERAL);
  }

  @Test
  public void regular_string_literal() throws Exception {
    assertLiteral("\"content\"", Kind.REGULAR_STRING_LITERAL);
  }

  @Test
  public void boolean_literal() throws Exception {
    assertLiteral("true", Kind.BOOLEAN_LITERAL);
  }

  @Test
  public void magic_constant_literal() throws Exception {
    assertLiteral("__FILE__", Kind.MAGIC_CONSTANT);
  }


  private void assertLiteral(String toParse, Kind expectedKind) throws Exception {
    LiteralTreeImpl tree = parse(toParse, PHPLexicalGrammar.COMMON_SCALAR);

    assertThat(tree.is(expectedKind)).isTrue();
    assertThat(tree.value()).isEqualTo(toParse);
  }
}
