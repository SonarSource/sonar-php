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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.PhpIssue;

public class FunctionComplexityCheckTest {

  private static final String FILE_NAME = "FunctionComplexityCheck.php";
  private FunctionComplexityCheck check = new FunctionComplexityCheck();

  @Test
  public void defaultValue() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_NAME), ImmutableList.<PhpIssue>of(
      new LegacyIssue(check, "The Cyclomatic Complexity of this function \"ko\" is 28 which is greater than 20 authorized.").line(3)));
  }

  @Test
  public void custom() throws Exception {
    check.threshold = 2;
    CheckVerifier.verify(check, FILE_NAME);
  }

}
