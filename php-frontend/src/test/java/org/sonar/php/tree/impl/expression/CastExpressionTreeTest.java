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
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.CastExpressionTree;
import org.sonar.plugins.php.api.tree.expression.PrefixedCastExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.assertj.core.api.Assertions.assertThat;

class CastExpressionTreeTest extends PHPTreeModelTest {

  @Test
  void castExpression() {
    CastExpressionTree tree = parse("(int)$a", PHPLexicalGrammar.CAST_TYPE);
    assertThat(tree.is(Kind.CAST_EXPRESSION)).isTrue();
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.castType().text()).isEqualTo("int");
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
    assertThat(expressionToString(tree.expression())).isEqualTo("$a");

    tree = parse("(real)$a", PHPLexicalGrammar.CAST_TYPE);
    assertThat(tree.is(Kind.CAST_EXPRESSION)).isTrue();
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.castType().text()).isEqualTo("real");
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
    assertThat(expressionToString(tree.expression())).isEqualTo("$a");
  }

  @Test
  void prefixedCastExpression() {
    PrefixedCastExpressionTree tree = parse("b'abc'", PHPLexicalGrammar.CAST_TYPE);
    assertThat(tree.is(Kind.PREFIXED_CAST_EXPRESSION)).isTrue();
    assertThat(expressionToString(tree)).isEqualTo("b'abc'");
    assertThat(tree.prefix().text()).isEqualTo("b");
    assertThat(expressionToString(tree.expression())).isEqualTo("'abc'");

    StringBuilder visitReport = new StringBuilder();
    tree.accept(new PHPVisitorCheck() {
      @Override
      public void visitPrefixedCastExpression(PrefixedCastExpressionTree tree) {
        visitReport.append("visitPrefixedCastExpression");
        super.visitPrefixedCastExpression(tree);
      }
    });
    assertThat(visitReport.toString()).hasToString("visitPrefixedCastExpression");
  }

}
