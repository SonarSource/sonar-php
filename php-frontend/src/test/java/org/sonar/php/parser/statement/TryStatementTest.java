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
package org.sonar.php.parser.statement;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

public class TryStatementTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.TRY_STATEMENT)
      .matches("try {}")
      .matches("try {} finally {}")
      .matches("try {} catch(Exception $e) {}")
      .matches("try {} catch(Exception1 $e1) {} catch(Exception2 $e2) {}")
      .matches("try {} catch(\\NS\\Exception $e1) {}")
      .notMatches("try {} catch(finally $e1) {}");
  }
}
