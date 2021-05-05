/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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

import java.util.Collections;
import org.junit.Test;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;

public class TooManyLinesInFileCheckTest {

  private TooManyLinesInFileCheck check = new TooManyLinesInFileCheck();
  private String fileName = "TooManyLinesInFileCheck.php";

  @Test
  public void defaultValue() throws Exception {
    CheckVerifier.verifyNoIssue(check, fileName);
  }

  @Test
  public void custom() throws Exception {
    check.max = 2;
    PHPCheckTest.check(check, TestUtils.getCheckFile(fileName), Collections.singletonList(new LegacyIssue(
      check,
      "File \"TooManyLinesInFileCheck.php\" has 3 lines, which is greater than " + check.max + " authorized. Split it into smaller files.")));
  }
}
