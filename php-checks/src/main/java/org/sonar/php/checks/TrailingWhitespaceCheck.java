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
package org.sonar.php.checks;

import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = TrailingWhitespaceCheck.KEY)
public class TrailingWhitespaceCheck extends PHPVisitorCheck {

  public static final String KEY = "S1131";
  private static final String MESSAGE = "Remove the useless trailing whitespaces at the end of this line.";

  private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[^" + LexicalConstant.WHITESPACE + "]+[" + LexicalConstant.WHITESPACE + "]+$");

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    Stream<String> lines = CheckUtils.lines(context().getPhpFile());

    Iterator<String> it = lines.iterator();
    int i = 0;
    while (it.hasNext()) {
      if (test(it.next())) {
        context().newLineIssue(this, i + 1, MESSAGE);
      }
      i++;
    }
  }

  private static boolean test(String line) {
    return line.length() > 0 && WHITESPACE_PATTERN.matcher(line).find();
  }

}
