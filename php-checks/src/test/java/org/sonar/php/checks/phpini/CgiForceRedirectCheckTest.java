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
package org.sonar.php.checks.phpini;

import java.io.File;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.php.ini.PhpIniCheck;

import static org.sonar.php.checks.phpini.PhpIniCheckTestUtils.check;

class CgiForceRedirectCheckTest {

  private PhpIniCheck check = new CgiForceRedirectCheck();
  private File dir = new File("src/test/resources/checks/phpini");

  @Test
  void lineIssue() throws Exception {
    check(check, new File(dir, "cgi_force_redirect.ini"));
  }

  @Test
  void emptyFile() throws Exception {
    check(check, new File(dir, "empty.ini"), Collections.emptyList());
  }

}
