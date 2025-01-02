/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.impl.expression.FunctionExpressionTreeImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
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
    if (inStaticContext && "$this".equals(varIdentifier.variableExpression().text()) && !isWithinNonStaticFunctionExpression(varIdentifier)) {
      context().newIssue(this, varIdentifier.variableExpression(), MESSAGE);
    }
    super.visitVariableIdentifier(varIdentifier);
  }

  private boolean isWithinNonStaticFunctionExpression(VariableIdentifierTree varIdentifier) {
    Tree parent = varIdentifier.getParent();
    while (parent != null) {
      if (parent.is(Tree.Kind.FUNCTION_EXPRESSION)) {
        FunctionExpressionTreeImpl functionExpressionTree = (FunctionExpressionTreeImpl) parent;
        return functionExpressionTree.staticToken() == null;
      }
      parent = parent.getParent();
    }

    return false;
  }
}
