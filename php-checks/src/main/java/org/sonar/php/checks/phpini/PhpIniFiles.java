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
import javax.annotation.Nullable;
import org.sonar.php.ini.PhpIniIssue;
import org.sonar.php.ini.tree.Directive;
import org.sonar.php.ini.tree.PhpIniFile;

import static org.sonar.php.ini.BasePhpIniIssue.newIssue;

public class PhpIniFiles {

  private PhpIniFiles() {
    // This class should only be called in a static way
  }

  public static List<PhpIniIssue> checkRequiredBoolean(PhpIniFile phpIniFile, String directiveName, PhpIniBoolean requiredValue, String directiveMessage) {
    return checkRequiredBoolean(phpIniFile, directiveName, requiredValue, directiveMessage, null);
  }

  public static List<PhpIniIssue> checkRequiredBoolean(PhpIniFile phpIniFile, String directiveName, PhpIniBoolean requiredValue,
    String directiveMessage, @Nullable String fileMessage) {

    List<Directive> directives = phpIniFile.directivesForName(directiveName);
    List<PhpIniIssue> issues = new ArrayList<>();

    for (Directive directive : directives) {
      if (!requiredValue.matchesValue(directive)) {
        issues.add(newIssue(directiveMessage).line(directive.name().line()));
      }
    }

    if (fileMessage != null && directives.isEmpty()) {
      issues.add(newIssue(fileMessage));
    }

    return issues;
  }

}
