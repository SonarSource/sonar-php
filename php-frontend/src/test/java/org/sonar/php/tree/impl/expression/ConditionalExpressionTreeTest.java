/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.tree.impl.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionalExpressionTreeTest extends PHPTreeModelTest {

  @Test
  void full() {
    ConditionalExpressionTree tree = parse("$condition ? $trueExpr : $falseExpr", Kind.CONDITIONAL_EXPRESSION);

    assertThat(tree.is(Kind.CONDITIONAL_EXPRESSION)).isTrue();
    assertThat(expressionToString(tree.condition())).isEqualTo("$condition");
    assertThat(tree.queryToken().text()).isEqualTo("?");
    assertThat(expressionToString(tree.trueExpression())).isEqualTo("$trueExpr");
    assertThat(tree.colonToken().text()).isEqualTo(":");
    assertThat(expressionToString(tree.falseExpression())).isEqualTo("$falseExpr");
  }

  @Test
  void withoutTrueExpression() {
    ConditionalExpressionTree tree = parse("$condition ? : $falseExpr", Kind.CONDITIONAL_EXPRESSION);

    assertThat(tree.is(Kind.CONDITIONAL_EXPRESSION)).isTrue();
    assertThat(expressionToString(tree.condition())).isEqualTo("$condition");
    assertThat(tree.queryToken().text()).isEqualTo("?");
    assertThat(tree.trueExpression()).isNull();
    assertThat(tree.colonToken().text()).isEqualTo(":");
    assertThat(expressionToString(tree.falseExpression())).isEqualTo("$falseExpr");
  }

}
