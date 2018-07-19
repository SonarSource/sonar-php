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
package org.sonar.plugins.php;

import java.io.File;
import org.sonar.plugins.php.api.tests.PHPCheckVerifier;
import org.sonar.plugins.php.api.visitors.PHPCheck;

public final class CheckVerifier extends PHPCheckVerifier {

  private CheckVerifier(boolean readExpectedIssuesFromComments) {
    super(readExpectedIssuesFromComments);
  }

  public static void verify(PHPCheck check, String relativePath) {
    PHPCheckVerifier.verify(checkFile(relativePath), check);
  }

  public static void verifyNoIssue(PHPCheck check, String relativePath) {
    PHPCheckVerifier.verifyNoIssue(checkFile(relativePath), check);
  }

  public static void verifyNoIssueIgnoringExpected(PHPCheck check, String relativePath) {
    new CheckVerifier(false).createVerifier(checkFile(relativePath), check).assertNoIssues();
  }

  private static File checkFile(String relativePath) {
    return new File("src/test/resources/checks/" + relativePath);
  }

}
