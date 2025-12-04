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
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionCallTreeTest extends PHPTreeModelTest {

  @Test
  void withoutArgument() {
    FunctionCallTree tree = parse("f()", PHPLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(Kind.FUNCTION_CALL)).isTrue();
    assertThat(expressionToString(tree.callee())).isEqualTo("f");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.callArguments()).isEmpty();
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
  }

  @Test
  void withArgument() {
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
  void withNamedArguments() {
    FunctionCallTree tree = parse("f(self::$p1, a: $p2)", PHPLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.callArguments()).hasSize(2);

    assertThat(tree.callArguments().get(0).name()).isNull();
    assertThat(tree.callArguments().get(1).name()).hasToString("a");
  }

  @Test
  void withNamedKeywordArgument() {
    FunctionCallTree tree = parse("f(if: $a)", PHPLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.callArguments()).hasSize(1);
    assertThat(tree.callArguments().get(0).name()).hasToString("if");
  }
}
