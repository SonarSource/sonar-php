/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.IssueLocation;
import org.sonar.plugins.php.api.visitors.LocationInFile;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1131")
public class TrailingWhitespaceCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Remove the useless trailing whitespaces at the end of this line.";

  private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[^" + LexicalConstant.WHITESPACE + "]+([" + LexicalConstant.WHITESPACE + "]+)$");

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    Stream<String> lines = CheckUtils.lines(context().getPhpFile());

    Iterator<String> it = lines.iterator();
    int lineNumber = 1;
    while (it.hasNext()) {
      checkLine(it.next(), lineNumber);
      lineNumber++;
    }
  }

  private void checkLine(String line, int lineNumber) {
    if (!line.isEmpty()) {
      Matcher m = WHITESPACE_PATTERN.matcher(line);
      if (m.find()) {
        context().newIssue(this, issueLocation(m, lineNumber));
      }
    }
  }

  private static IssueLocation issueLocation(Matcher m, int lineNumber) {
    LocationInFile location = new LocationInFileImpl(null, lineNumber, m.start(1), lineNumber, m.end(1));
    return new IssueLocation(location, MESSAGE);
  }
}
