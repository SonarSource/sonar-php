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

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = NonLFCharAsEOLCheck.KEY)
public class NonLFCharAsEOLCheck extends PHPVisitorCheck {

  public static final String KEY = "S1779";
  private static final String MESSAGE = "Replace all non line feed end of line characters in this file \"%s\" by LF.";

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    String contents = context().getPhpFile().contents();

    for (int i = 0; i < contents.length(); i++) {
      char c = contents.charAt(i);

      if (c == '\r' || c == '\u2028' || c == '\u2029') {
        String message = String.format(MESSAGE, context().getPhpFile().filename());
        context().newFileIssue(this, message);
        break;
      }
    }
  }

}
