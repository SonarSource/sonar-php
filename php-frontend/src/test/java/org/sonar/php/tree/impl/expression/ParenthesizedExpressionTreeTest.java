/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;

import static org.assertj.core.api.Assertions.assertThat;

class ParenthesizedExpressionTreeTest extends PHPTreeModelTest {

  @Test
  void parenthesizedExpression() {
    ParenthesisedExpressionTree tree = parse("($a)", Kind.PARENTHESISED_EXPRESSION);

    assertThat(tree.is(Kind.PARENTHESISED_EXPRESSION)).isTrue();
    assertThat(tree.openParenthesis().text()).isEqualTo("(");
    assertThat(expressionToString(tree.expression())).isEqualTo("$a");
    assertThat(tree.closeParenthesis().text()).isEqualTo(")");
  }

  @Test
  void parenthesizedYieldExpression() {
    ParenthesisedExpressionTree tree = parse("(yield $a)", Kind.PARENTHESISED_EXPRESSION);

    assertThat(tree.is(Kind.PARENTHESISED_EXPRESSION)).isTrue();
    assertThat(tree.openParenthesis().text()).isEqualTo("(");
    assertThat(expressionToString(tree.expression())).isEqualTo("yield $a");
    assertThat(tree.closeParenthesis().text()).isEqualTo(")");
  }

}
