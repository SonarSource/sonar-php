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

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.PhpIssue;

public class TooManyFunctionParametersCheckTest {

  private static final String FILE_NAME = "TooManyFunctionParametersCheck.php";

  private TooManyFunctionParametersCheck check = new TooManyFunctionParametersCheck();

  @Test
  public void default_parameter_values() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_NAME), issues(3, 14, 24));
  }

  @Test
  public void custom_value_for_max() throws Exception {
    check.max = 2;
    CheckVerifier.verify(check, FILE_NAME);
  }

  @Test
  public void custom_value_for_constructor_max() throws Exception {
    check.constructorMax = 2;
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_NAME), issues(3, 14, 19, 24, 29));
  }

  private List<PhpIssue> issues(int... lines) {
    List<PhpIssue> list = new ArrayList<>();
    for (int line : lines) {
      list.add(new LegacyIssue(check, null).line(line));
    }
    return list;
  }

}
