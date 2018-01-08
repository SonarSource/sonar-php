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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;

import static org.assertj.core.api.Assertions.assertThat;

public class ConditionalExpressionTreeTest extends PHPTreeModelTest {

  @Test
  public void full() throws Exception {
    ConditionalExpressionTree tree = parse("$condition ? $trueExpr : $falseExpr", Kind.CONDITIONAL_EXPRESSION);

    assertThat(tree.is(Kind.CONDITIONAL_EXPRESSION)).isTrue();
    assertThat(expressionToString(tree.condition())).isEqualTo("$condition");
    assertThat(tree.queryToken().text()).isEqualTo("?");
    assertThat(expressionToString(tree.trueExpression())).isEqualTo("$trueExpr");
    assertThat(tree.colonToken().text()).isEqualTo(":");
    assertThat(expressionToString(tree.falseExpression())).isEqualTo("$falseExpr");
  }

  @Test
  public void without_true_expression() throws Exception {
    ConditionalExpressionTree tree = parse("$condition ? : $falseExpr", Kind.CONDITIONAL_EXPRESSION);

    assertThat(tree.is(Kind.CONDITIONAL_EXPRESSION)).isTrue();
    assertThat(expressionToString(tree.condition())).isEqualTo("$condition");
    assertThat(tree.queryToken().text()).isEqualTo("?");
    assertThat(tree.trueExpression()).isNull();
    assertThat(tree.colonToken().text()).isEqualTo(":");
    assertThat(expressionToString(tree.falseExpression())).isEqualTo("$falseExpr");
  }

}
