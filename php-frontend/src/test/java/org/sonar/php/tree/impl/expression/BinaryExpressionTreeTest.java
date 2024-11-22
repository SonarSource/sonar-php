/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.sslr.grammar.GrammarRuleKey;

import static org.assertj.core.api.Assertions.assertThat;

class BinaryExpressionTreeTest extends PHPTreeModelTest {

  @Test
  void conditionalOr() {
    testBinary(Kind.CONDITIONAL_OR, "||");
    testBinary(Kind.ALTERNATIVE_CONDITIONAL_OR, "or");
  }

  @Test
  void conditionalXor() {
    testBinary(Kind.ALTERNATIVE_CONDITIONAL_XOR, "xor");
  }

  @Test
  void conditionalAnd() {
    testBinary(Kind.CONDITIONAL_AND, "&&");
    testBinary(Kind.ALTERNATIVE_CONDITIONAL_AND, "and");
  }

  @Test
  void bitwiseOr() {
    testBinary(Kind.BITWISE_OR, "|");
  }

  @Test
  void bitwiseXor() {
    testBinary(Kind.BITWISE_XOR, "^");
  }

  @Test
  void bitwiseAnd() {
    testBinary(Kind.BITWISE_AND, "&");
  }

  @Test
  void equality() {
    testBinary(Kind.STRICT_NOT_EQUAL_TO, "!==", PHPLexicalGrammar.EQUALITY_EXPR);
    testBinary(Kind.NOT_EQUAL_TO, "!=", PHPLexicalGrammar.EQUALITY_EXPR);
    testBinary(Kind.STRICT_EQUAL_TO, "===", PHPLexicalGrammar.EQUALITY_EXPR);
    testBinary(Kind.EQUAL_TO, "==", PHPLexicalGrammar.EQUALITY_EXPR);
    testBinary(Kind.ALTERNATIVE_NOT_EQUAL_TO, "<>", PHPLexicalGrammar.EQUALITY_EXPR);
    testBinary(Kind.COMPARISON, "<=>", PHPLexicalGrammar.EQUALITY_EXPR);
  }

  @Test
  void relational() {
    testBinary(Kind.LESS_THAN_OR_EQUAL_TO, "<=", PHPLexicalGrammar.RELATIONAL_EXPR);
    testBinary(Kind.GREATER_THAN_OR_EQUAL_TO, ">=", PHPLexicalGrammar.RELATIONAL_EXPR);
    testBinary(Kind.LESS_THAN, "<", PHPLexicalGrammar.RELATIONAL_EXPR);
    testBinary(Kind.GREATER_THAN, ">", PHPLexicalGrammar.RELATIONAL_EXPR);
  }

  @Test
  void shift() {
    testBinary(Kind.LEFT_SHIFT, "<<", PHPLexicalGrammar.SHIFT_EXPR);
    testBinary(Kind.RIGHT_SHIFT, ">>", PHPLexicalGrammar.SHIFT_EXPR);
  }

  @Test
  void additive() {
    testBinary(Kind.PLUS, "+", PHPLexicalGrammar.ADDITIVE_EXPR);
    testBinary(Kind.MINUS, "-", PHPLexicalGrammar.ADDITIVE_EXPR);
  }

  @Test
  void multiplicative() {
    testBinary(Kind.MULTIPLY, "*", PHPLexicalGrammar.MULTIPLICATIVE_EXPR);
    testBinary(Kind.DIVIDE, "/", PHPLexicalGrammar.MULTIPLICATIVE_EXPR);
    testBinary(Kind.REMAINDER, "%", PHPLexicalGrammar.MULTIPLICATIVE_EXPR);
  }

  @Test
  void power() {
    testBinary(Kind.POWER, "**", PHPLexicalGrammar.POWER_EXPR);
  }

  @Test
  void concatenation() {
    testBinary(Kind.CONCATENATION, ".", PHPLexicalGrammar.ADDITIVE_EXPR);
  }

  @Test
  void instanceofExpr() {
    testBinary(Kind.INSTANCE_OF, "instanceof", PHPLexicalGrammar.POSTFIX_EXPR);
  }

  private void testBinary(Kind kind, String operator) {
    BinaryExpressionTree tree = parse("$a " + operator + " $b", kind);

    assertThat(tree.is(kind)).isTrue();
    assertThat(tree.leftOperand().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.leftOperand())).isEqualTo("$a");
    assertThat(tree.operator().text()).isEqualTo(operator);
    assertThat(tree.rightOperand().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.rightOperand())).isEqualTo("$b");
  }

  private void testBinary(Kind kind, String operator, GrammarRuleKey ruleKey) {
    BinaryExpressionTree tree = parse("$a " + operator + " $b", ruleKey);

    assertThat(tree.is(kind)).isTrue();
    assertThat(tree.leftOperand().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.leftOperand())).isEqualTo("$a");
    assertThat(tree.operator().text()).isEqualTo(operator);
    assertThat(tree.rightOperand().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.rightOperand())).isEqualTo("$b");
  }

  @Test
  void testAssociativityOr() {
    Kind conditionalOr = Kind.CONDITIONAL_OR;
    BinaryExpressionTree tree = parse("$a || $b || $c", conditionalOr);

    assertThat(tree.is(conditionalOr)).isTrue();
    assertThat(tree.leftOperand().is(conditionalOr)).isTrue();
    assertThat(expressionToString(tree.leftOperand())).isEqualTo("$a || $b");
    assertThat(tree.rightOperand().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.rightOperand())).isEqualTo("$c");
  }

  @Test
  void testAssociativityNullCoalescing() {
    Kind coalescingExpr = Kind.NULL_COALESCING_EXPRESSION;
    BinaryExpressionTree tree = parse("$a ?? $b ?? $c", coalescingExpr);

    assertThat(tree.is(coalescingExpr)).isTrue();
    assertThat(tree.leftOperand().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.leftOperand())).isEqualTo("$a");
    assertThat(tree.rightOperand().is(coalescingExpr)).isTrue();
    assertThat(expressionToString(tree.rightOperand())).isEqualTo("$b ?? $c");
  }

}
