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
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;

import static org.fest.assertions.Assertions.assertThat;

public class AssignmentExpressionTreeTest extends PHPTreeModelTest {

  @Test
  public void test() throws Exception {
    AssignmentExpressionTree tree = parse("$a %= $b", PHPLexicalGrammar.ASSIGNMENT_EXPRESSION);
    assertThat(tree.is(Kind.REMAINDER_ASSIGNMENT)).isTrue();
    assertThat(tree.variable().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.variable())).isEqualTo("$a");
    assertThat(tree.equalToken().text()).isEqualTo("%=");
    assertThat(tree.value().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(expressionToString(tree.value())).isEqualTo("$b");
  }

}
