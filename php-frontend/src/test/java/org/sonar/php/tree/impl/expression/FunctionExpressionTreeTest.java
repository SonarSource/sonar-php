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
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionExpressionTreeTest extends PHPTreeModelTest {

  @Test
  void basic() {
    FunctionExpressionTree tree = parse("function () {}", Kind.FUNCTION_EXPRESSION);

    assertThat(tree.is(Kind.FUNCTION_EXPRESSION)).isTrue();
    assertThat(tree.staticToken()).isNull();
    assertThat(tree.functionToken().text()).isEqualTo("function");
    assertThat(tree.referenceToken()).isNull();
    assertThat(tree.parameters().parameters()).isEmpty();
    assertThat(tree.lexicalVars()).isNull();
    assertThat(tree.returnTypeClause()).isNull();
    assertThat(expressionToString(tree.body())).isEqualTo("{}");
  }

  @Test
  void withReference() {
    FunctionExpressionTree tree = parse("function &() {}", Kind.FUNCTION_EXPRESSION);

    assertThat(tree.is(Kind.FUNCTION_EXPRESSION)).isTrue();
    assertThat(tree.staticToken()).isNull();
    assertThat(tree.functionToken().text()).isEqualTo("function");
    assertThat(tree.referenceToken().text()).isEqualTo("&");
    assertThat(tree.parameters().parameters()).isEmpty();
    assertThat(tree.lexicalVars()).isNull();
    assertThat(tree.returnTypeClause()).isNull();
    assertThat(expressionToString(tree.body())).isEqualTo("{}");
  }

  @Test
  void staticFunction() {
    FunctionExpressionTree tree = parse("static function () {}", Kind.FUNCTION_EXPRESSION);

    assertThat(tree.is(Kind.FUNCTION_EXPRESSION)).isTrue();
    assertThat(tree.staticToken().text()).isEqualTo("static");
    assertThat(tree.functionToken().text()).isEqualTo("function");
    assertThat(tree.referenceToken()).isNull();
    assertThat(tree.parameters().parameters()).isEmpty();
    assertThat(tree.lexicalVars()).isNull();
    assertThat(tree.returnTypeClause()).isNull();
    assertThat(expressionToString(tree.body())).isEqualTo("{}");
  }

  @Test
  void withLexicalVars() {
    FunctionExpressionTree tree = parse("function () use ($a) {}", Kind.FUNCTION_EXPRESSION);

    assertThat(tree.is(Kind.FUNCTION_EXPRESSION)).isTrue();
    assertThat(tree.staticToken()).isNull();
    assertThat(tree.functionToken().text()).isEqualTo("function");
    assertThat(tree.referenceToken()).isNull();
    assertThat(tree.parameters().parameters()).isEmpty();
    assertThat(tree.lexicalVars()).isNotNull();
    assertThat(tree.returnTypeClause()).isNull();
    assertThat(expressionToString(tree.body())).isEqualTo("{}");
  }

  @Test
  void withReturnType() {
    FunctionExpressionTree tree = parse("function () : bool {}", Kind.FUNCTION_EXPRESSION);

    assertThat(tree.is(Kind.FUNCTION_EXPRESSION)).isTrue();
    assertThat(tree.staticToken()).isNull();
    assertThat(tree.functionToken().text()).isEqualTo("function");
    assertThat(tree.referenceToken()).isNull();
    assertThat(tree.parameters().parameters()).isEmpty();
    assertThat(tree.lexicalVars()).isNull();
    assertThat(tree.returnTypeClause()).isNotNull();
    assertThat(expressionToString(tree.body())).isEqualTo("{}");
  }

}
