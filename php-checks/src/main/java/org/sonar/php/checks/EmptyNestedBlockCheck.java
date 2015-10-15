/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseTraitDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import javax.annotation.Nullable;
import java.util.List;

@Rule(
  key = EmptyNestedBlockCheck.KEY,
  name = "Nested blocks of code should not be left empty",
  priority = Priority.MAJOR,
  tags = Tags.BUG)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("5min")
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
    check(tree.statements(), tree.closeCurlyBraceToken(), tree);
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    super.visitSwitchStatement(tree);
    check(tree.cases(), tree.closeCurlyBraceToken(), tree.openCurlyBraceToken());
  }

  @Override
  public void visitUseTraitDeclaration(UseTraitDeclarationTree tree) {
    super.visitUseTraitDeclaration(tree);
    check(tree.adaptations(), tree.closeCurlyBraceToken(), tree.openCurlyBraceToken());
  }

  private <T extends Tree> void check(List<T> statements, @Nullable SyntaxToken lastToken, Tree issueTree) {
    if (statements.isEmpty() && lastToken != null && lastToken.trivias().isEmpty()) {
      context().newIssue(KEY, MESSAGE).tree(issueTree);
    }
  }

}
