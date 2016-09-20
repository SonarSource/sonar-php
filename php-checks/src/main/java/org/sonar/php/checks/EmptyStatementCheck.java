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
import org.sonar.plugins.php.api.tree.statement.EmptyStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;

@Rule(
  key = EmptyStatementCheck.KEY,
  name = "Empty statements should be removed",
  priority = Priority.MINOR,
  tags = {Tags.CERT, Tags.UNUSED, Tags.MISRA})
@ActivatedByDefault
@SqaleConstantRemediation("2min")
public class EmptyStatementCheck extends PHPVisitorCheck {

  public static final String KEY = "S1116";
  private static final String MESSAGE = "Remove this empty statement.";

  @Override
  public void visitEmptyStatement(EmptyStatementTree tree) {
    context().newIssue(this, MESSAGE).tree(tree);

    super.visitEmptyStatement(tree);
  }

}
