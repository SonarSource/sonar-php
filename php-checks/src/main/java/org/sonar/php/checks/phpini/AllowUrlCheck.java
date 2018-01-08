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
package org.sonar.php.checks.phpini;

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.ini.PhpIniCheck;
import org.sonar.php.ini.PhpIniIssue;
import org.sonar.php.ini.tree.PhpIniFile;

import static org.sonar.php.checks.phpini.PhpIniFiles.checkRequiredBoolean;

@Rule(key = "S3334")
public class AllowUrlCheck implements PhpIniCheck {

  @Override
  public List<PhpIniIssue> analyze(PhpIniFile phpIniFile) {
    List<PhpIniIssue> issues = new ArrayList<>();
    issues.addAll(checkRequiredBoolean(
      phpIniFile,
      "allow_url_include",
      PhpIniBoolean.OFF,
      "Disable \"allow_url_include\"."));
    issues.addAll(checkRequiredBoolean(
      phpIniFile,
      "allow_url_fopen",
      PhpIniBoolean.OFF,
      "Disable \"allow_url_fopen\".",
      "Disable \"allow_url_fopen\" explicitly; it is enabled by default."));
    return issues;
  }

}
