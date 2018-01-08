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
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerBracketTree;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrayInitializerBracketTreeTest extends PHPTreeModelTest {

  @Test
  public void one_element() throws Exception {
    ArrayInitializerBracketTree tree = parse("[0]", PHPLexicalGrammar.ARRAY_INIALIZER);

    assertThat(tree.is(Kind.ARRAY_INITIALIZER_BRACKET)).isTrue();

    assertThat(tree.openBracketToken().text()).isEqualTo("[");
    assertThat(tree.arrayPairs()).hasSize(1);
    assertThat(expressionToString(tree.arrayPairs().get(0).value())).isEqualTo("0");
    assertThat(tree.closeBracketToken().text()).isEqualTo("]");
  }

  @Test
  public void multiple_elements() throws Exception {
    ArrayInitializerBracketTree tree = parse("[0, 1, 2]", PHPLexicalGrammar.ARRAY_INIALIZER);

    assertThat(tree.is(Kind.ARRAY_INITIALIZER_BRACKET)).isTrue();

    assertThat(tree.openBracketToken().text()).isEqualTo("[");

    assertThat(tree.arrayPairs()).hasSize(3);
    assertThat(tree.arrayPairs().getSeparators()).hasSize(2);
    assertThat(expressionToString(tree.arrayPairs().get(0))).isEqualTo("0");

    assertThat(tree.closeBracketToken().text()).isEqualTo("]");
  }

  @Test
  public void with_trailing_comma() throws Exception {
    ArrayInitializerBracketTree tree = parse("[0, 1, 2,]", PHPLexicalGrammar.ARRAY_INIALIZER);

    assertThat(tree.is(Kind.ARRAY_INITIALIZER_BRACKET)).isTrue();

    assertThat(tree.openBracketToken().text()).isEqualTo("[");

    assertThat(tree.arrayPairs()).hasSize(3);
    assertThat(tree.arrayPairs().getSeparators()).hasSize(3);
    assertThat(expressionToString(tree.arrayPairs().get(0))).isEqualTo("0");

    assertThat(tree.closeBracketToken().text()).isEqualTo("]");
  }

}
