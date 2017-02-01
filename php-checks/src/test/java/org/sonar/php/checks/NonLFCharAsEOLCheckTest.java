/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.php.FileTestUtils;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;

public class NonLFCharAsEOLCheckTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private NonLFCharAsEOLCheck check = new NonLFCharAsEOLCheck();
  private PhpFile ok_file;
  private PhpFile ko_file;

  @Before
  public void setUp() throws Exception {
    ok_file = FileTestUtils.getFile(temporaryFolder.newFile(), "<?php $foo = 1; \n");
    ko_file = FileTestUtils.getFile(temporaryFolder.newFile(), "<?php $foo = 1; \r\n");
  }

  @Test
  public void ok() throws IOException {
    PHPCheckTest.check(check, ok_file);
  }

  @Test
  public void ko() throws IOException {
    ImmutableList<PhpIssue> issues = ImmutableList.<PhpIssue>of(
      new LegacyIssue(check, "Replace all non line feed end of line characters in this file \"" + ko_file.relativePath().getFileName() + "\" by LF."));
    PHPCheckTest.check(check, ko_file, issues);
  }

}
