/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.ini.PhpIniCheck;
import org.sonar.php.ini.PhpIniIssue;
import org.sonar.php.ini.tree.PhpIniFile;

import static org.sonar.php.checks.phpini.PhpIniFiles.checkRequiredBoolean;

@Rule(key = "S3337")
public class EnableDlCheck implements PhpIniCheck {

  private static final String FILE_MESSAGE = "Explicitly set \"enable_dl\" to 0.";
  private static final String DIRECTIVE_MESSAGE = "Update this \"enable_dl\" configuration to turn it off.";

  @Override
  public List<PhpIniIssue> analyze(PhpIniFile phpIniFile) {
    return checkRequiredBoolean(phpIniFile, "enable_dl", PhpIniBoolean.OFF, DIRECTIVE_MESSAGE, FILE_MESSAGE);
  }

}
