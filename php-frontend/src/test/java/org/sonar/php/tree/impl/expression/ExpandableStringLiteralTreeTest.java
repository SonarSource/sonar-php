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
import org.sonar.plugins.php.api.tree.expression.ExecutionOperatorTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;

import static org.assertj.core.api.Assertions.assertThat;

class ExpandableStringLiteralTreeTest extends PHPTreeModelTest {

  @Test
  void simpleVariable() {
    ExpandableStringLiteralTree tree = parse("\"simple var: $a\"", Kind.EXPANDABLE_STRING_LITERAL);

    assertExpandableStringLiteral(tree, 1, 1);

    assertFirstString(tree, "simple var: ");
    assertFirstExpression(tree, "$a", Kind.VARIABLE_IDENTIFIER);
  }

  @Test
  void semiComplexVariable() {
    ExpandableStringLiteralTree tree = parse("\"semi-complex var: ${$a}\"", Kind.EXPANDABLE_STRING_LITERAL);

    assertExpandableStringLiteral(tree, 1, 1);

    assertFirstString(tree, "semi-complex var: ");
    assertFirstExpression(tree, "${$a}", Kind.COMPOUND_VARIABLE_NAME);
  }

  @Test
  void complexVariable() {
    ExpandableStringLiteralTree tree = parse("\"complex var: {$a}\"", Kind.EXPANDABLE_STRING_LITERAL);

    assertExpandableStringLiteral(tree, 1, 1);

    assertFirstString(tree, "complex var: ");
    assertFirstExpression(tree, "{$a}", Kind.COMPUTED_VARIABLE_NAME);
  }

  @Test
  void multipleVariablesAndStrings() {
    ExpandableStringLiteralTree tree = parse("\"1st var: $a - 2nd composed vars: $b$c\"", Kind.EXPANDABLE_STRING_LITERAL);

    assertExpandableStringLiteral(tree, 2, 3);

    assertFirstString(tree, "1st var: ");
    assertFirstExpression(tree, "$a", Kind.VARIABLE_IDENTIFIER);
  }

  @Test
  void testPseudoComment() {
    ExpandableStringLiteralTree tree = parse("\"/**/{$a}\"", Kind.EXPANDABLE_STRING_LITERAL);
    assertFirstExpression(tree, "{$a}", Kind.COMPUTED_VARIABLE_NAME);
  }

  @Test
  void executionOperator() {
    ExecutionOperatorTree tree = parse("`ls $a`", PHPLexicalGrammar.EXPRESSION);

    ExpandableStringLiteralTree literal = tree.literal();

    assertThat(literal.openDoubleQuoteToken().text()).isEqualTo("`");
    assertThat(literal.strings()).hasSize(1);
    assertThat(literal.expressions()).hasSize(1);
    assertThat(literal.closeDoubleQuoteToken().text()).isEqualTo("`");

    assertFirstString(literal, "ls ");
    assertFirstExpression(literal, "$a", Kind.VARIABLE_IDENTIFIER);

    // without embedded expression
    tree = parse("`ls dir`", PHPLexicalGrammar.EXPRESSION);
    literal = tree.literal();
    assertThat(literal.expressions()).isEmpty();
    assertFirstString(literal, "ls dir");
  }

  private static void assertExpandableStringLiteral(ExpandableStringLiteralTree tree, int stringsSize, int expressionsSize) {
    assertThat(tree.is(Kind.EXPANDABLE_STRING_LITERAL)).isTrue();

    assertThat(tree.openDoubleQuoteToken().text()).isEqualTo("\"");
    assertThat(tree.strings()).hasSize(stringsSize);
    assertThat(tree.expressions()).hasSize(expressionsSize);
    assertThat(tree.closeDoubleQuoteToken().text()).isEqualTo("\"");
  }

  private static void assertFirstExpression(ExpandableStringLiteralTree tree, String s, Kind kind) {
    ExpressionTree expr = tree.expressions().get(0);

    assertThat(expr.is(kind)).isTrue();
    assertThat(expressionToString(expr)).isEqualTo(s);
  }

  private static void assertFirstString(ExpandableStringLiteralTree tree, String s) {
    ExpandableStringCharactersTree string = tree.strings().get(0);

    assertThat(string.is(Kind.EXPANDABLE_STRING_CHARACTERS)).isTrue();
    assertThat(string.value()).isEqualTo(s);
  }

}
