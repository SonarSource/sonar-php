/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PhpIssue;

class TooManyFunctionParametersCheckTest {

  private static final String FILE_NAME = "TooManyFunctionParametersCheck.php";

  private TooManyFunctionParametersCheck check = new TooManyFunctionParametersCheck();

  @Test
  void shouldVerifyDefaultParameterValues() {
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_NAME), issues(3, 14, 24, 36, 61, 78, 92, 106, 120));
  }

  @Test
  void shouldVerifyCustomValueForMax() {
    check.max = 2;
    CheckVerifier.verify(check, FILE_NAME);
  }

  @Test
  void shouldVerifyCustomValueForConstructorMax() {
    check.constructorMax = 2;
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_NAME), issues(3, 14, 19, 24, 29, 36, 61, 78, 92, 106, 120));
  }

  private List<PhpIssue> issues(int... lines) {
    List<PhpIssue> list = new ArrayList<>();
    for (int line : lines) {
      list.add(new LineIssue(check, line, null));
    }
    return list;
  }

}
