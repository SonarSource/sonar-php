/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;

@Rule(
  key = TooManyLinesInFileCheck.KEY,
  name = "Files should not have too many lines",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@ActivatedByDefault
@SqaleConstantRemediation("1h")
public class TooManyLinesInFileCheck extends PHPVisitorCheck {

  public static final String KEY = "S104";

  private static final String MESSAGE = "File \"%s\" has %s lines, which is greater than %s authorized. Split it into smaller files.";

  private static final int DEFAULT = 1000;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  public int max = DEFAULT;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    int numberOfLines = tree.eofToken().line();
    if (numberOfLines > max) {
      context().newIssue(this, String.format(MESSAGE, context().file().getName(), numberOfLines, max));
    }
  }

}
