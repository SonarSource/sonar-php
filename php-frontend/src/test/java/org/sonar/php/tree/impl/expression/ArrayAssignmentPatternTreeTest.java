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
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternTree;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrayAssignmentPatternTreeTest extends PHPTreeModelTest {

  @Test
  public void simple() throws Exception {
    ArrayAssignmentPatternTree tree = parse("[$a, , $b]", Kind.ARRAY_ASSIGNMENT_PATTERN);

    assertThat(tree.is(Kind.ARRAY_ASSIGNMENT_PATTERN)).isTrue();

    assertThat(tree.openBracketToken().text()).isEqualTo("[");
    assertThat(tree.elements()).hasSize(3);
    assertThat(tree.separators()).hasSize(2);
    assertThat(expressionToString(tree.elements().get(0).get())).isEqualTo("$a");
    assertThat(tree.elements().get(1)).isEmpty();
    assertThat(expressionToString(tree.elements().get(2).get())).isEqualTo("$b");
    assertThat(tree.closeBracketToken().text()).isEqualTo("]");
  }

}
