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
import org.sonar.php.ini.tree.Directive;
import org.sonar.php.ini.tree.PhpIniFile;

import static org.sonar.php.ini.BasePhpIniIssue.newIssue;

@Rule(key = "S3333")
public class OpenBasedirCheck implements PhpIniCheck {

  private static final String FILE_MESSAGE = "Set \"open_basedir\".";
  private static final String DIRECTIVE_MESSAGE = "Limit \"open_basedir\" to a narrower path than \"%s\".";

  @Override
  public List<PhpIniIssue> analyze(PhpIniFile phpIniFile) {
    List<PhpIniIssue> issues = new ArrayList<>();
    List<Directive> directives = phpIniFile.directivesForName("open_basedir");

    if (directives.isEmpty()) {
      issues.add(newIssue(FILE_MESSAGE));
    }

    for (Directive directive : directives) {

      String value = directive.value().text();
      if (value.startsWith("\"")) {
        value = value.substring(1);
      }
      if (value.endsWith("\"")) {
        value = value.substring(0, value.length() - 1);
      }

      for (String path : value.split(":|;")) {
        if (".".equals(path) || "/".equals(path)) {
          String message = String.format(DIRECTIVE_MESSAGE, path);
          int line = directive.name().line();
          issues.add(newIssue(message).line(line));
          break;
        }
      }

    }

    return issues;
  }

}
