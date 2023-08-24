/*
 * SonarQube PHP Custom Rules Example
 * Copyright (C) 2016-2016 SonarSource SA
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
package org.sonar.samples.php.checks;

import org.junit.Test;
import org.sonar.plugins.php.api.tests.PhpTestFile;
import org.sonar.plugins.php.api.tests.PHPCheckTest;

import java.io.File;

/**
 * Test class to test the check implementation.
 */
public class OtherForbiddenFunctionUseCheckTest {

  @Test
  public void test() throws Exception {
    PHPCheckTest.check(new OtherForbiddenFunctionUseCheck(), new PhpTestFile(new File("src/test/resources/checks/forbiddenFunctionUseCheck.php")));
  }

}
