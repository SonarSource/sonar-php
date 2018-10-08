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
package org.sonar.php.parser.expression;

import org.junit.Test;
import org.sonar.plugins.php.api.tree.Tree;

import static org.sonar.php.utils.Assertions.assertThat;

public class AlternativeLogicalOperatorsTest {

  @Test
  public void test_and() {
    assertThat(Tree.Kind.ALTERNATIVE_CONDITIONAL_AND)
      .matches("$a = $b and $b")
      .matches("$a = $b and $b")
      .matches("$a = $b and $a = $b");
  }

  @Test
  public void test_or() {
    assertThat(Tree.Kind.ALTERNATIVE_CONDITIONAL_OR)
      .matches("$a = $b or $b")
      .matches("$a = $b or $b xor $b")
      .matches("$a = $b or $a = $b");
  }

  @Test
  public void test_xor() {
    assertThat(Tree.Kind.ALTERNATIVE_CONDITIONAL_XOR)
      .matches("$a = $b xor $b")
      .matches("$a = $b xor $b and $b")
      .matches("$a = $b xor $a = $b");
  }
}
