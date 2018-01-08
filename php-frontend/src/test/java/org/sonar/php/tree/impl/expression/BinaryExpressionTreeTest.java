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
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.sslr.grammar.GrammarRuleKey;

import static org.assertj.core.api.Assertions.assertThat;

public class BinaryExpressionTreeTest extends PHPTreeModelTest {

  @Test
  public void conditional_or() throws Exception {
    testBinary(Kind.CONDITIONAL_OR, "||");
    testBinary(Kind.ALTERNATIVE_CONDITIONAL_OR, "or");
  }

  @Test
  public void conditional_xor() throws Exception {
    testBinary(Kind.ALTERNATIVE_CONDITIONAL_XOR, "xor");
  }

  @Test
  public void conditional_and() throws Exception {
    testBinary(Kind.CONDITIONAL_AND, "&&");
    testBinary(Kind.ALTERNATIVE_CONDITIONAL_AND, "and");
  }

  @Test
  public void bitwise_or() throws Exception {
    testBinary(Kind.BITWISE_OR, "|");
  }

  @Test
  public void bitwise_xor() throws Exception {
    testBinary(Kind.BITWISE_XOR, "^");
  }

  @Test
  public void bitwise_and() throws Exception {
    testBinary(Kind.BITWISE_AND, "&");
  }

  @Test
  public void equality() throws Exception {
    testBinary(Kind.STRICT_NOT_EQUAL_TO, "!==", PHPLexicalGrammar.EQUALITY_EXPR);
    testBinary(Kind.NOT_EQUAL_TO, "!=", PHPLexicalGrammar.EQUALITY_EXPR);
    testBinary(Kind.STRICT_EQUAL_TO, "===", PHPLexicalGrammar.EQUALITY_EXPR);
    testBinary(Kind.EQUAL_TO, "==", PHPLexicalGrammar.EQUALITY_EXPR);
    testBinary(Kind.ALTERNATIVE_NOT_EQUAL_TO, "<>", PHPLexicalGrammar.EQUALITY_EXPR);
    testBinary(Kind.COMPARISON, "<=>", PHPLexicalGrammar.EQUALITY_EXPR);
  }

  @Test
  public void relational() throws Exception {
    testBinary(Kind.LESS_THAN_OR_EQUAL_TO, "<=", PHPLexicalGrammar.RELATIONAL_EXPR);
    testBinary(Kind.GREATER_THAN_OR_EQUAL_TO, ">=", PHPLexicalGrammar.RELATIONAL_EXPR);
    testBinary(Kind.LESS_THAN, "<", PHPLexicalGrammar.RELATIONAL_EXPR);
    testBinary(Kind.GREATER_THAN, ">", PHPLexicalGrammar.RELATIONAL_EXPR);
  }

  @Test
  public void shift() throws Exception {
    testBinary(Kind.LEFT_SHIFT, "<<", PHPLexicalGrammar.SHIFT_EXPR);
    testBinary(Kind.RIGHT_SHIFT, ">>", PHPLexicalGrammar.SHIFT_EXPR);
  }

  @Test
  public void additive() throws Exception {
    testBinary(Kind.PLUS, "+", PHPLexicalGrammar.ADDITIVE_EXPR);
    testBinary(Kind.MINUS, "-", PHPLexicalGrammar.ADDITIVE_EXPR);
  }

  @Test
  public void multiplicative() throws Exception {
    testBinary(Kind.MULTIPLY, "*", PHPLexicalGrammar.MULTIPLICATIVE_EXPR);
    testBinary(Kind.DIVIDE, "/", PHPLexicalGrammar.MULTIPLICATIVE_EXPR);
    testBinary(Kind.REMAINDER, "%", PHPLexicalGrammar.MULTIPLICATIVE_EXPR);
  }

  @Test
  public void power() throws Exception {
    testBinary(Kind.POWER, "**", PHPLexicalGrammar.POWER_EXPR);
  }

  @Test
  public void concatenation() throws Exception {
    testBinary(Kind.CONCATENATION, ".", PHPLexicalGrammar.ADDITIVE_EXPR);
  }

  @Test
  public void instanceof_expr() throws Exception {
    testBinary(Kind.INSTANCE_OF, "instanceof", PHPLexicalGrammar.POSTFIX_EXPR);
  }

  private void testBinary(Kind kind, String operator) throws Exception {
    BinaryExpressionTree tree = parse("$a " + operator + " $b", kind);

    assertThat(tree.is(kind)).isTrue();
    assertThat(tree.leftOperand().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.leftOperand())).isEqualTo("$a");
    assertThat(tree.operator().text()).isEqualTo(operator);
    assertThat(tree.rightOperand().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.rightOperand())).isEqualTo("$b");
  }

  private void testBinary(Kind kind, String operator, GrammarRuleKey ruleKey) throws Exception {
    BinaryExpressionTree tree = parse("$a " + operator + " $b", ruleKey);

    assertThat(tree.is(kind)).isTrue();
    assertThat(tree.leftOperand().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.leftOperand())).isEqualTo("$a");
    assertThat(tree.operator().text()).isEqualTo(operator);
    assertThat(tree.rightOperand().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.rightOperand())).isEqualTo("$b");
  }

  @Test
  public void test_associativity_or() throws Exception {
    Kind conditionalOr = Kind.CONDITIONAL_OR;
    BinaryExpressionTree tree = parse("$a || $b || $c", conditionalOr);

    assertThat(tree.is(conditionalOr)).isTrue();
    assertThat(tree.leftOperand().is(conditionalOr)).isTrue();
    assertThat(expressionToString(tree.leftOperand())).isEqualTo("$a || $b");
    assertThat(tree.rightOperand().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.rightOperand())).isEqualTo("$c");
  }

  @Test
  public void test_associativity_null_coalescing() throws Exception {
    Kind coalescingExpr = Kind.NULL_COALESCING_EXPRESSION;
    BinaryExpressionTree tree = parse("$a ?? $b ?? $c", coalescingExpr);

    assertThat(tree.is(coalescingExpr)).isTrue();
    assertThat(tree.leftOperand().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.leftOperand())).isEqualTo("$a");
    assertThat(tree.rightOperand().is(coalescingExpr)).isTrue();
    assertThat(expressionToString(tree.rightOperand())).isEqualTo("$b ?? $c");
  }

}
