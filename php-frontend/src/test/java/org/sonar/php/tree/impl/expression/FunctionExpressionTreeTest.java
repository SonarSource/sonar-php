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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;

import static org.assertj.core.api.Assertions.assertThat;

public class FunctionExpressionTreeTest extends PHPTreeModelTest {

  @Test
  public void basic() throws Exception {
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
  public void with_reference() throws Exception {
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
  public void static_function() throws Exception {
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
  public void with_lexical_vars() throws Exception {
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
  public void with_return_type() throws Exception {
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
