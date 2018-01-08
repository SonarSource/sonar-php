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
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;

import static org.assertj.core.api.Assertions.assertThat;

public class PrefixExpressionTreeTest extends PHPTreeModelTest {

  @Test
  public void unary_plus() throws Exception {
    testPrefixExpr(Kind.UNARY_PLUS, "+");
  }

  @Test
  public void unary_minus() throws Exception {
    testPrefixExpr(Kind.UNARY_MINUS, "-");
  }

  @Test
  public void prefix_inc() throws Exception {
    testPrefixExpr(Kind.PREFIX_INCREMENT, "++");
  }

  @Test
  public void prefix_dec() throws Exception {
    testPrefixExpr(Kind.PREFIX_DECREMENT, "--");
  }

  @Test
  public void bitwise_complement() throws Exception {
    testPrefixExpr(Kind.BITWISE_COMPLEMENT, "~");
  }

  @Test
  public void logical_complement() throws Exception {
    testPrefixExpr(Kind.LOGICAL_COMPLEMENT, "!");
  }

  @Test
  public void error_control() throws Exception {
    testPrefixExpr(Kind.ERROR_CONTROL, "@");
  }

  private void testPrefixExpr(Kind kind, String operator) throws Exception {
    UnaryExpressionTree tree = parse(operator + "$a", PHPLexicalGrammar.UNARY_EXPR);

    assertThat(tree.is(kind)).isTrue();
    assertThat(tree.operator().text()).isEqualTo(operator);
    assertThat(expressionToString(tree.expression())).isEqualTo("$a");
  }

}
