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
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;

public class NonLFCharAsEOLCheckTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private NonLFCharAsEOLCheck check = new NonLFCharAsEOLCheck();
  private PhpFile okFile;
  private PhpFile koFile;

  @Before
  public void setUp() throws Exception {
    okFile = TestUtils.getFile(temporaryFolder.newFile(), "<?php $foo = 1; \n");
    koFile = TestUtils.getFile(temporaryFolder.newFile(), "<?php $foo = 1; \r\n");
  }

  @Test
  public void ok() {
    PHPCheckTest.check(check, okFile, Collections.emptyList());
  }

  @Test
  public void ko() {
    List<PhpIssue> issues = Collections.singletonList(
      new LegacyIssue(check, "Replace all non line feed end of line characters in this file \"" + koFile.filename() + "\" by LF."));
    PHPCheckTest.check(check, koFile, issues);
  }

}
