/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
import org.sonar.php.tree.visitors.PHPIssue;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PhpCheckTestUtils;
import org.sonar.plugins.php.api.visitors.Issue;

public class TooManyFunctionParametersCheckTest {

  private static final String FILE_NAME = "TooManyFunctionParametersCheck.php";

  private TooManyFunctionParametersCheck check = new TooManyFunctionParametersCheck();

  @Test
  public void default_parameter_values() throws Exception {
    PhpCheckTestUtils.check(check, TestUtils.getCheckFile(FILE_NAME), issues(3, 13, 23));
  }

  @Test
  public void custom_value_for_max() throws Exception {
    check.max = 2;
    PhpCheckTestUtils.check(check, TestUtils.getCheckFile(FILE_NAME));
  }

  @Test
  public void custom_value_for_constructor_max() throws Exception {
    check.constructorMax = 2;
    PhpCheckTestUtils.check(check, TestUtils.getCheckFile(FILE_NAME), issues(3, 13, 18, 23, 28));
  }

  private List<Issue> issues(int... lines) {
    List<Issue> list = new ArrayList<>();
    for (int line : lines) {
      list.add(new PHPIssue(check, null).line(line));
    }
    return list;
  }

}
