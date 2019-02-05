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
package org.sonar.php.parser.expression;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

public class ArrayDestructuringAssignmentTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.ARRAY_DESTRUCTURING_ASSIGNMENT)
      .matches("[$a, $b] = $array")
      .matches("[$a, &$b] = $array")
      .matches("[&$a, $b,, [&$c, $d]] = $array")
      .matches("[&$one, [$two, &$three]] = $a")
      .matches("['one' => &$one, 'two' => $two] = $a")
      .notMatches("$array = [1, 2]")
      .notMatches("list($a, &$b) = $array")
    ;
  }

}
