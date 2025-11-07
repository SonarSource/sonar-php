/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import java.io.File;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;

class NonLFCharAsEOLCheckTest {

  @TempDir
  public File temporaryFolder;

  private NonLFCharAsEOLCheck check = new NonLFCharAsEOLCheck();
  private PhpFile okFile;
  private PhpFile koFile;

  @BeforeEach
  public void setUp() {
    okFile = TestUtils.getFile(new File(temporaryFolder, "test1.php"), "<?php $foo = 1; \n");
    koFile = TestUtils.getFile(new File(temporaryFolder, "test2.php"), "<?php $foo = 1; \r\n");
  }

  @Test
  void ok() {
    PHPCheckTest.check(check, okFile, Collections.emptyList());
  }

  @Test
  void ko() {
    List<PhpIssue> issues = Collections.singletonList(
      new FileIssue(check, "Replace all non line feed end of line characters in this file \"" + koFile.filename() + "\" by LF."));
    PHPCheckTest.check(check, koFile, issues);
  }

}
