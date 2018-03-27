/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2018 SonarSource SA
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
package org.sonar.samples.php;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;

@Rule(
  key = "visitor",
  priority = Priority.MINOR,
  name = "PHP visitor check",
  description = "desc")
@SqaleConstantRemediation("5min")
public class LegacyCustomPHPVisitorCheck extends PHPVisitorCheck {

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    context().newIssue(this, "Function expression.").tree(tree);
    super.visitFunctionExpression(tree);
  }

}
