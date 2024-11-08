/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class NewExpressionTreeTest extends PHPTreeModelTest {

  @Test
  void shouldParseNewExpression() {
    NewExpressionTree tree = parse("new Foo", Kind.NEW_EXPRESSION);

    assertThat(tree.is(Kind.NEW_EXPRESSION)).isTrue();

    assertThat(tree.newToken().text()).isEqualTo("new");
    assertThat(expressionToString(tree.expression())).isEqualTo("Foo");
  }

  /* New PHP 8.4 feature: new without parentheses and subsequent access behind */
  @ParameterizedTest
  @CsvSource(value = {
    "new MyClass()->method();,            MyClass()",
    "new $className()->method();,         $className()",
    "new (trim(' MyClass '))->method();,  (trim(' MyClass '))",
    "new class { /* … */ }->method();,    class { /* … */ }",
    "new class () { /* … */ }->method();, class () { /* … */ }"
  }, delimiter = ',')
  void shouldSupportNewObjectExpressionWithMethodCallBehind(String code, String newExpressionContent) {
    ExpressionStatementTree tree = parse(code, PHPLexicalGrammar.STATEMENT);
    FunctionCallTree functionCall = (FunctionCallTree) tree.expression();
    assertThat(functionCall.callee().is(Kind.OBJECT_MEMBER_ACCESS)).isTrue();
    MemberAccessTree memberAccess = (MemberAccessTree) functionCall.callee();

    assertThat(memberAccess.member().is(Kind.NAME_IDENTIFIER)).isTrue();
    NameIdentifierTree nameIdentifier = (NameIdentifierTree) memberAccess.member();
    assertThat(nameIdentifier.token().text()).isEqualTo("method");

    assertThat(memberAccess.object().is(Kind.NEW_EXPRESSION)).isTrue();
    NewExpressionTree newExpression = (NewExpressionTree) memberAccess.object();
    assertThat(expressionToString(newExpression.expression())).isEqualTo(newExpressionContent);
  }

  @Test
  void shouldSupportNewObjectWithoutParenthesisStaticPropertyAccess() {
    ExpressionStatementTree tree = parse("new MyClass()::$staticProperty;", PHPLexicalGrammar.STATEMENT);
    MemberAccessTree memberAccess = (MemberAccessTree) tree.expression();

    assertThat(memberAccess.member().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    VariableIdentifierTree variableIdentifier = (VariableIdentifierTree) memberAccess.member();
    assertThat(variableIdentifier.token().text()).isEqualTo("$staticProperty");

    assertThat(memberAccess.object().is(Kind.NEW_EXPRESSION)).isTrue();
    NewExpressionTree newExpression = (NewExpressionTree) memberAccess.object();
    assertThat(expressionToString(newExpression.expression())).isEqualTo("MyClass()");
  }

  @Test
  void shouldSupportNewObjectWithoutParenthesisSubsequentCallToInvoke() {
    ExpressionStatementTree tree = parse("new MyClass()();", PHPLexicalGrammar.STATEMENT);
    FunctionCallTree functionCall = (FunctionCallTree) tree.expression();
    assertThat(functionCall.callee().is(Kind.NEW_EXPRESSION)).isTrue();
    NewExpressionTree newExpression = (NewExpressionTree) functionCall.callee();
    assertThat(expressionToString(newExpression.expression())).isEqualTo("MyClass()");
  }

  @Test
  void shouldSupportNewObjectWithoutParenthesisArrayAccess() {
    ExpressionStatementTree tree = parse("new MyClass()[0];", PHPLexicalGrammar.STATEMENT);
    ArrayAccessTree arrayAccess = (ArrayAccessTree) tree.expression();
    assertThat(arrayAccess.offset().is(Kind.NUMERIC_LITERAL)).isTrue();

    LiteralTree numericLiteral = (LiteralTree) arrayAccess.offset();
    assertThat(numericLiteral.value()).isEqualTo("0");

    assertThat(arrayAccess.object().is(Kind.NEW_EXPRESSION)).isTrue();
    NewExpressionTree newExpression = (NewExpressionTree) arrayAccess.object();
    assertThat(expressionToString(newExpression.expression())).isEqualTo("MyClass()");
  }

  @Test
  void shouldSupportNewObjectWithoutParenthesisVariableAsClassName() {
    ExpressionStatementTree tree = parse("new $className()->property;", PHPLexicalGrammar.STATEMENT);
    MemberAccessTree memberAccess = (MemberAccessTree) tree.expression();

    assertThat(memberAccess.member().is(Kind.NAME_IDENTIFIER)).isTrue();
    NameIdentifierTree nameIdentifier = (NameIdentifierTree) memberAccess.member();
    assertThat(nameIdentifier.token().text()).isEqualTo("property");

    assertThat(memberAccess.object().is(Kind.NEW_EXPRESSION)).isTrue();
    NewExpressionTree newExpression = (NewExpressionTree) memberAccess.object();
    assertThat(expressionToString(newExpression.expression())).isEqualTo("$className()");
  }

  // Documented use-case: this is an invalid code which produce an incorrect tree, but still doesn't result in parsing error.
  @Test
  void shouldSupportInvalidCode() {
    ExpressionStatementTree tree = parse("new MyClass->method();", PHPLexicalGrammar.STATEMENT);
    NewExpressionTree newExpression = (NewExpressionTree) tree.expression();
    assertThat(expressionToString(newExpression.expression())).isEqualTo("MyClass->method()");
  }
}
