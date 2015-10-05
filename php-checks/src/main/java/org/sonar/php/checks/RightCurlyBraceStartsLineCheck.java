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
import org.sonar.php.checks.utils.TokenVisitor;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseTraitDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = RightCurlyBraceStartsLineCheck.KEY,
  name = "A close curly brace should be located at the beginning of a line",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("1min")
public class RightCurlyBraceStartsLineCheck extends PHPVisitorCheck {

  public static final String KEY = "S1109";
  private static final String MESSAGE = "Move this closing curly brace to the next line.";

  @Override
  public void visitBlock(BlockTree tree) {
    super.visitBlock(tree);
    SyntaxToken prevToken = tree.openCurlyBraceToken();
    List<StatementTree> statements = tree.statements();
    if (!statements.isEmpty()) {
      prevToken = ((PHPTree) statements.get(statements.size() - 1)).getLastToken();
    }
    checkCloseCurlyBrace(tree.closeCurlyBraceToken(), tree.openCurlyBraceToken(), prevToken);
  }

  @Override
  public void visitUseTraitDeclaration(UseTraitDeclarationTree tree) {
    super.visitUseTraitDeclaration(tree);

    if (tree.openCurlyBraceToken() != null) {
      checkCloseCurlyBrace(
        tree.closeCurlyBraceToken(),
        tree.openCurlyBraceToken(),
        new TokenVisitor(tree).prevToken(tree.closeCurlyBraceToken()));
    }
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    super.visitSwitchStatement(tree);
    if (tree.is(Kind.SWITCH_STATEMENT)) {
      checkCloseCurlyBrace(
        tree.closeCurlyBraceToken(),
        tree.openCurlyBraceToken(),
        new TokenVisitor(tree).prevToken(tree.closeCurlyBraceToken()));
    }
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);
    checkCloseCurlyBrace(
      tree.closeCurlyBraceToken(),
      tree.openCurlyBraceToken(),
      new TokenVisitor(tree).prevToken(tree.closeCurlyBraceToken()));
  }


  private void checkCloseCurlyBrace(SyntaxToken rBrace, SyntaxToken lBrace, SyntaxToken prevToken) {
    int rightBraceLine = rBrace.line();
    if (rightBraceLine == lBrace.line()) {
      return;
    }

    if (rightBraceLine == prevToken.line()) {
      context().newIssue(KEY, MESSAGE).line(rightBraceLine);
    }
  }

}
