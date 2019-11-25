/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.BuiltInTypeTree;
import org.sonar.plugins.php.api.tree.expression.ArrowFunctionExpressionTree;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrowFunctionExpressionTreeTest extends PHPTreeModelTest {

  @Test
  public void basic() throws Exception {
    ArrowFunctionExpressionTree tree = parse("fn () => NULL", Tree.Kind.ARROW_FUNCTION_EXPRESSION);

    assertThat(tree.is(Tree.Kind.ARROW_FUNCTION_EXPRESSION)).isTrue();
    assertThat(tree.staticToken()).isNull();
    assertThat(tree.functionToken().text()).isEqualTo("fn");
    assertThat(tree.referenceToken()).isNull();
    assertThat(tree.parameters().parameters()).isEmpty();
    assertThat(tree.returnTypeClause()).isNull();
    assertThat(tree.doubleArrowToken().text()).isEqualTo("=>");
    assertThat(expressionToString(tree.body())).isEqualTo("NULL");
  }

  @Test
  public void with_static_reference_parameter_return_type() throws Exception {
    ArrowFunctionExpressionTree tree = parse("static fn&($x): string => $x . '_'", Tree.Kind.ARROW_FUNCTION_EXPRESSION);

    assertThat(tree.is(Tree.Kind.ARROW_FUNCTION_EXPRESSION)).isTrue();
    assertThat(tree.staticToken().text()).isEqualTo("static");
    assertThat(tree.functionToken().text()).isEqualTo("fn");
    assertThat(tree.referenceToken().text()).isEqualTo("&");
    assertThat(tree.parameters().parameters()).hasSize(1);
    assertThat(tree.parameters().parameters().get(0).variableIdentifier().text()).isEqualTo("$x");
    assertThat(((BuiltInTypeTree)tree.returnTypeClause().type().typeName()).token().text()).isEqualTo("string");
    assertThat(tree.doubleArrowToken().text()).isEqualTo("=>");
    assertThat(expressionToString(tree.body())).isEqualTo("$x . '_'");
  }

}
