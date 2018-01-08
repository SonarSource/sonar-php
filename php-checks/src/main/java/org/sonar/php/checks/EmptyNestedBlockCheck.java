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

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseTraitDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = EmptyNestedBlockCheck.KEY)
public class EmptyNestedBlockCheck extends PHPVisitorCheck {

  public static final String KEY = "S108";
  private static final String MESSAGE = "Either remove or fill this block of code.";

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (tree.body().is(Tree.Kind.BLOCK)) {
      super.visitBlock((BlockTree) tree.body());
    }
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    super.visitBlock(tree.body());
  }

  @Override
  public void visitBlock(BlockTree tree) {
    super.visitBlock(tree);
    if (isEmpty(tree.statements(), tree.closeCurlyBraceToken())) {
      context().newIssue(this, tree, MESSAGE);
    }
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    super.visitSwitchStatement(tree);
    if (isEmpty(tree.cases(), tree.closeCurlyBraceToken())) {
      context().newIssue(this, tree.openCurlyBraceToken(), tree.closeCurlyBraceToken(), MESSAGE);
    }
  }

  @Override
  public void visitUseTraitDeclaration(UseTraitDeclarationTree tree) {
    super.visitUseTraitDeclaration(tree);
    if (isEmpty(tree.adaptations(), tree.closeCurlyBraceToken())) {
      context().newIssue(this, tree.openCurlyBraceToken(), tree.closeCurlyBraceToken(), MESSAGE);
    }
  }

  private static <T extends Tree> boolean isEmpty(List<T> statements, @Nullable SyntaxToken lastToken) {
    return statements.isEmpty() && lastToken != null && lastToken.trivias().isEmpty();
  }

}
