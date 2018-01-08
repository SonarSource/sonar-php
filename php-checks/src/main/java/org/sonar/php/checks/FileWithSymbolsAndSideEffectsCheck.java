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
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.InlineHTMLTree;
import org.sonar.plugins.php.api.tree.statement.UnsetVariableStatementTree;
import org.sonar.plugins.php.api.tree.statement.YieldStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = FileWithSymbolsAndSideEffectsCheck.KEY)
public class FileWithSymbolsAndSideEffectsCheck extends PHPVisitorCheck {

  public static final String KEY = "S2036";
  private static final String MESSAGE = "Refactor this file to either declare symbols or cause side effects, but not both.";

  private boolean fileHasSymbol;
  private boolean fileHasSideEffect;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    fileHasSymbol = false;
    fileHasSideEffect = false;

    super.visitCompilationUnit(tree);

    if (fileHasSymbol && fileHasSideEffect) {
      context().newFileIssue(this, MESSAGE);
    }
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    if (tree.is(Kind.CLASS_DECLARATION, Kind.INTERFACE_DECLARATION)) {
      fileHasSymbol = true;
      // do not enter inside class declaration
    }
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    fileHasSymbol = true;
    // do not enter inside function declaration
  }

  @Override
  public void visitYieldStatement(YieldStatementTree tree) {
    super.visitYieldStatement(tree);
    fileHasSideEffect = true;
  }

  @Override
  public void visitInlineHTML(InlineHTMLTree tree) {
    if (!CheckUtils.isClosingTag(tree.inlineHTMLToken())) {
      fileHasSideEffect = true;
    }
  }

  @Override
  public void visitUnsetVariableStatement(UnsetVariableStatementTree tree) {
    super.visitUnsetVariableStatement(tree);
    fileHasSideEffect = true;
  }

  @Override
  public void visitExpressionStatement(ExpressionStatementTree tree) {
    super.visitExpressionStatement(tree);

    if (tree.expression().is(Tree.Kind.FUNCTION_CALL)) {
      FunctionCallTree functionCallTree = (FunctionCallTree)tree.expression();

      String callee = functionCallTree.callee().toString();
      if ("define".equalsIgnoreCase(callee)) {
        return;
      }
    }

    fileHasSideEffect = true;
  }

}
