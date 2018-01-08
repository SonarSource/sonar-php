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
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ThisVariableUsageInStaticContextCheck.KEY)
public class ThisVariableUsageInStaticContextCheck extends PHPVisitorCheck {

  public static final String KEY = "S2014";

  private static final String MESSAGE = "Remove this use of \"$this\".";

  private boolean inStaticContext = false;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    inStaticContext = false;
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree methodDec) {
    inStaticContext = CheckUtils.hasModifier(methodDec.modifiers(), "static");
    super.visitMethodDeclaration(methodDec);
  }

  @Override
  public void visitVariableIdentifier(VariableIdentifierTree varIdentifier) {
    if (inStaticContext && "$this".equals(varIdentifier.variableExpression().text())) {
      context().newIssue(this, varIdentifier.variableExpression(), MESSAGE);
    }
    super.visitVariableIdentifier(varIdentifier);
  }

}
