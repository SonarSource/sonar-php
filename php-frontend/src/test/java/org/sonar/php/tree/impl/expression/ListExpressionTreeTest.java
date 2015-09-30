/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.impl.expression;

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ListExpressionTree;

import static org.fest.assertions.Assertions.assertThat;

public class ListExpressionTreeTest extends PHPTreeModelTest {

  @Test
  public void empty() throws Exception {
    ListExpressionTree tree = parse("list ()", Kind.LIST_EXPRESSION);

    assertListExpression(tree, 0, 0);
  }

  @Test
  public void simple_variable() throws Exception {
    ListExpressionTree tree = parse("list ($a, $b)", Kind.LIST_EXPRESSION);

    assertListExpression(tree, 2, 1);
    assertFirstElement(tree, Kind.VARIABLE_IDENTIFIER, "$a");
  }

  @Test
  public void omitted_element() throws Exception {
    ListExpressionTree tree = parse("list (, $a, , ,$b)", Kind.LIST_EXPRESSION);

    assertListExpression(tree, 5, 4);
    assertFirstElement(tree, Kind.SKIPPED_LIST_ELEMENT, "");
    assertThat(expressionToString(Iterables.getLast(tree.elements()))).isEqualTo("$b");
  }

  @Test
  public void nested_list_expression() throws Exception {
    ListExpressionTree tree = parse("list (list ($a), $b)", Kind.LIST_EXPRESSION);

    assertListExpression(tree, 2, 1);
    assertFirstElement(tree, Kind.LIST_EXPRESSION, "list ($a)");
  }

  private void assertFirstElement(ListExpressionTree tree, Kind kind, String string) {
    ExpressionTree element = tree.elements().get(0);
    assertThat(element.is(kind)).isTrue();
    assertThat(expressionToString(element)).isEqualTo(string);
  }

  private void assertListExpression(ListExpressionTree tree, int nbElement, int nbSeparators) {
    assertThat(tree.is(Kind.LIST_EXPRESSION)).isTrue();

    assertThat(tree.listToken().text()).isEqualTo("list");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.elements()).hasSize(nbElement);
    assertThat(tree.elements().getSeparators()).hasSize(nbSeparators);
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
  }

}
