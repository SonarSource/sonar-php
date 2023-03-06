/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

public class FunctionCallTreeTest extends PHPTreeModelTest {

  @Test
  public void without_argument() throws Exception {
    FunctionCallTree tree = parse("f()", PHPLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(Kind.FUNCTION_CALL)).isTrue();
    assertThat(expressionToString(tree.callee())).isEqualTo("f");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.callArguments()).isEmpty();
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
  }

  @Test
  public void with_argument() throws Exception {
    FunctionCallTree tree = parse("f($p1, $p2)", PHPLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(Kind.FUNCTION_CALL)).isTrue();
    assertThat(expressionToString(tree.callee())).isEqualTo("f");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");

    assertThat(tree.callArguments()).hasSize(2);
    assertThat(tree.callArguments().getSeparators()).hasSize(1);
    assertThat(expressionToString(tree.callArguments().get(0).value())).isEqualTo("$p1");
    assertThat(expressionToString(tree.callArguments().get(1).value())).isEqualTo("$p2");

    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
  }


  @Test
  public void with_named_arguments() throws Exception {
    FunctionCallTree tree = parse("f(self::$p1, a: $p2)", PHPLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.callArguments()).hasSize(2);

    assertThat(tree.callArguments().get(0).name()).isNull();
    assertThat(tree.callArguments().get(1).name()).hasToString("a");
  }

  @Test
  public void with_named_keyword_argument() throws Exception {
    FunctionCallTree tree = parse("f(if: $a)", PHPLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.callArguments()).hasSize(1);
    assertThat(tree.callArguments().get(0).name()).hasToString("if");
  }
}
