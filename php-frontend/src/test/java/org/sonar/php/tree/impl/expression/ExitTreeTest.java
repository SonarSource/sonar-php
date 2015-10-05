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

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExitTree;

import static org.fest.assertions.Assertions.assertThat;

public class ExitTreeTest extends PHPTreeModelTest {

  @Test
  public void exit() throws Exception {
    ExitTree tree = parse("exit", Kind.EXIT_EXPRESSION);

    assertThat(tree.is(Kind.EXIT_EXPRESSION)).isTrue();
    assertThat(tree.wordToken().text()).isEqualTo("exit");
    assertThat(tree.openParenthesisToken()).isNull();
    assertThat(tree.parameterExpression()).isNull();
    assertThat(tree.closeParenthesisToken()).isNull();
  }

  @Test
  public void die() throws Exception {
    ExitTree tree = parse("die", Kind.EXIT_EXPRESSION);

    assertThat(tree.is(Kind.EXIT_EXPRESSION)).isTrue();
    assertThat(tree.wordToken().text()).isEqualTo("die");
    assertThat(tree.openParenthesisToken()).isNull();
    assertThat(tree.parameterExpression()).isNull();
    assertThat(tree.closeParenthesisToken()).isNull();
  }

  @Test
  public void with_parenthesis() throws Exception {
    ExitTree tree = parse("exit()", Kind.EXIT_EXPRESSION);

    assertThat(tree.is(Kind.EXIT_EXPRESSION)).isTrue();
    assertThat(tree.wordToken().text()).isEqualTo("exit");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.parameterExpression()).isNull();
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
  }

  @Test
  public void with_argument() throws Exception {
    ExitTree tree = parse("exit(1)", Kind.EXIT_EXPRESSION);

    assertThat(tree.is(Kind.EXIT_EXPRESSION)).isTrue();
    assertThat(tree.wordToken().text()).isEqualTo("exit");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(expressionToString(tree.parameterExpression())).isEqualTo("1");
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
  }

}
