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

public class MoreThanOneClassInFileCheckTest {

  private static final String TEST_DIR = "MoreThanOneClassInFileCheck/";
  private final MoreThanOneClassInFileCheck check = new MoreThanOneClassInFileCheck();

  @Test
  public void ok() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok.php");
  }

  @Test
  public void ko1() throws Exception {
    String message = "There are 2 independent classes in this file; move all but one of them to other files.";
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko1.php"), ImmutableList.<PhpIssue>of(new LegacyIssue(check, message).cost(1.0)));
  }

  @Test
  public void ko2() throws Exception {
    String message = "There are 2 independent interfaces in this file; move all but one of them to other files.";
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko2.php"), ImmutableList.<PhpIssue>of(new LegacyIssue(check, message).cost(1.0)));
  }

  @Test
  public void ko3() throws Exception {
    String message = "There are 1 independent classes and 2 independent interfaces in this file; move all but one of them to other files.";
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko3.php"), ImmutableList.<PhpIssue>of(new LegacyIssue(check, message).cost(2.0)));
  }

}
