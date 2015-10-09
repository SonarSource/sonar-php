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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.sonar.php.tree.visitors.PHPIssue;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.Issue;

public class ConstantNameCheckTest {

  private static final String FILE_NAME = "ConstantNameCheck.php";

  private ConstantNameCheck check = new ConstantNameCheck();

  @Test
  public void defaultValue() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_NAME));
  }

  @Test
  public void custom() throws Exception {
    check.format = "^[A-Z][a-z]*$";
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_NAME), ImmutableList.<Issue>of(
      new PHPIssue(ConstantNameCheck.KEY, "Rename this constant \"FOO\" to match the regular expression " + check.format + ".").line(8),
      new PHPIssue(ConstantNameCheck.KEY, null).line(11),
      new PHPIssue(ConstantNameCheck.KEY, null).line(15)));
  }
}
