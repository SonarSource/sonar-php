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
import java.util.List;
import org.junit.Test;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.PhpIssue;

public class TooManyFieldsInClassCheckTest {

  private TooManyFieldsInClassCheck check = new TooManyFieldsInClassCheck();
  private static final String fileName = "TooManyFieldsInClassCheck.php";

  @Test
  public void test_default() throws Exception {
    CheckVerifier.verifyNoIssueIgnoringExpected(check, fileName);
  }

  @Test
  public void custom_maximum_field_threshold() throws Exception {
    check.maximumFieldThreshold = 4;
    CheckVerifier.verify(check, fileName);
  }

  @Test
  public void custom_both_parameters() throws Exception {
    check.maximumFieldThreshold = 2;
    check.countNonpublicFields = false;

    List<PhpIssue> issues = ImmutableList.<PhpIssue>of(
      new LegacyIssue(check, "Refactor this class so it has no more than 2 public fields, rather than the 3 it currently has.").line(3),
      new LegacyIssue(check, "Refactor this class so it has no more than 2 public fields, rather than the 3 it currently has.").line(18)
    );
    PHPCheckTest.check(check, TestUtils.getCheckFile(fileName), issues);
  }
}
