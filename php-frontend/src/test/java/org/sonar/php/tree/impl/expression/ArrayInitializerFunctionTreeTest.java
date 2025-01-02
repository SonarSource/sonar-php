/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerFunctionTree;

import static org.assertj.core.api.Assertions.assertThat;

class ArrayInitializerFunctionTreeTest extends PHPTreeModelTest {

  @Test
  void empty() {
    ArrayInitializerFunctionTree tree = parse("array()", PHPLexicalGrammar.ARRAY_INIALIZER);

    assertThat(tree.is(Kind.ARRAY_INITIALIZER_FUNCTION)).isTrue();

    assertThat(tree.arrayToken().text()).isEqualTo("array");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.arrayPairs()).isEmpty();
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
  }

  @Test
  void nonEmpty() {
    ArrayInitializerFunctionTree tree = parse("array($a, $b, $c)", PHPLexicalGrammar.ARRAY_INIALIZER);

    assertThat(tree.is(Kind.ARRAY_INITIALIZER_FUNCTION)).isTrue();

    assertThat(tree.arrayToken().text()).isEqualTo("array");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");

    assertThat(tree.arrayPairs()).hasSize(3);
    assertThat(tree.arrayPairs().getSeparators()).hasSize(2);
    assertThat(expressionToString(tree.arrayPairs().get(0))).isEqualTo("$a");

    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
  }

  @Test
  void withTrailingComma() {
    ArrayInitializerFunctionTree tree = parse("array($a,)", PHPLexicalGrammar.ARRAY_INIALIZER);

    assertThat(tree.is(Kind.ARRAY_INITIALIZER_FUNCTION)).isTrue();

    assertThat(tree.arrayToken().text()).isEqualTo("array");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");

    assertThat(tree.arrayPairs()).hasSize(1);
    assertThat(tree.arrayPairs().getSeparators()).hasSize(1);
    assertThat(expressionToString(tree.arrayPairs().get(0))).isEqualTo("$a");

    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
  }

}
