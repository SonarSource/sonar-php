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
import org.sonar.php.checks.utils.TokenVisitor;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.DeclareStatementTree;
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseTraitDeclarationTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = LeftCurlyBraceStartsLineCheck.KEY)
public class LeftCurlyBraceStartsLineCheck extends PHPVisitorCheck {

  public static final String KEY = "S1106";
  private static final String MESSAGE = "Move this open curly brace to the beginning of next line.";

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);
    checkClass(tree);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    super.visitAnonymousClass(tree);
    checkClass(tree);
  }

  private void checkClass(ClassTree tree) {
    checkOpenCurlyBrace(
      tree.openCurlyBraceToken(),
      tree.closeCurlyBraceToken(),
      new TokenVisitor(tree).prevToken(tree.openCurlyBraceToken()));
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    super.visitFunctionDeclaration(tree);
    checkBlock(tree.body(), getLastToken(tree.parameters()));
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    super.visitFunctionExpression(tree);
    checkBlock(tree.body(), getLastToken(tree.parameters()));
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    super.visitMethodDeclaration(tree);
    checkBlock(tree.body(), getLastToken(tree.parameters()));
  }

  @Override
  public void visitUseTraitDeclaration(UseTraitDeclarationTree tree) {
    super.visitUseTraitDeclaration(tree);

    if (tree.openCurlyBraceToken() != null) {
      checkOpenCurlyBrace(
        tree.openCurlyBraceToken(),
        tree.closeCurlyBraceToken(),
        new TokenVisitor(tree).prevToken(tree.openCurlyBraceToken()));
    }
  }

  @Override
  public void visitDeclareStatement(DeclareStatementTree tree) {
    super.visitDeclareStatement(tree);
    if (tree.is(Kind.DECLARE_STATEMENT) && tree.statements().size() == 1) {
      checkBlock(tree.statements().get(0), tree.closeParenthesisToken());
    }
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    super.visitSwitchStatement(tree);
    if (tree.is(Kind.SWITCH_STATEMENT)) {
      checkOpenCurlyBrace(
        tree.openCurlyBraceToken(),
        tree.closeCurlyBraceToken(),
        new TokenVisitor(tree).prevToken(tree.openCurlyBraceToken()));
    }
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    super.visitIfStatement(tree);
    if (tree.is(Tree.Kind.IF_STATEMENT)) {
      checkBlock(tree.statements().get(0), getLastToken(tree.condition()));
    }
  }

  @Override
  public void visitElseifClause(ElseifClauseTree tree) {
    super.visitElseifClause(tree);
    if (tree.is(Tree.Kind.ELSEIF_CLAUSE)) {
      checkBlock(tree.statements().get(0), getLastToken(tree.condition()));
    }
  }

  @Override
  public void visitElseClause(ElseClauseTree tree) {
    super.visitElseClause(tree);
    if (tree.is(Tree.Kind.ELSE_CLAUSE)) {
      checkBlock(tree.statements().get(0), getLastToken(tree.elseToken()));
    }
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    super.visitForStatement(tree);
    if (tree.is(Tree.Kind.FOR_STATEMENT)) {
      checkBlock(tree.statements().get(0), tree.closeParenthesisToken());
    }
  }

  @Override
  public void visitForEachStatement(ForEachStatementTree tree) {
    super.visitForEachStatement(tree);
    if (tree.is(Tree.Kind.FOREACH_STATEMENT)) {
      checkBlock(tree.statements().get(0), tree.closeParenthesisToken());
    }
  }

  @Override
  public void visitDoWhileStatement(DoWhileStatementTree tree) {
    super.visitDoWhileStatement(tree);
    checkBlock(tree.statement(), tree.doToken());
  }

  @Override
  public void visitWhileStatement(WhileStatementTree tree) {
    super.visitWhileStatement(tree);
    if (tree.is(Tree.Kind.WHILE_STATEMENT)) {
      checkBlock(tree.statements().get(0), getLastToken(tree.condition()));
    }
  }

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    super.visitCatchBlock(tree);
    checkBlock(tree.block(), tree.closeParenthesisToken());
  }

  @Override
  public void visitTryStatement(TryStatementTree tree) {
    super.visitTryStatement(tree);
    checkBlock(tree.block(), tree.tryToken());
    if (tree.finallyToken() != null) {
      checkBlock(tree.finallyBlock(), tree.finallyToken());
    }
  }

  @Override
  public void visitNamespaceStatement(NamespaceStatementTree tree) {
    super.visitNamespaceStatement(tree);
    if (tree.openCurlyBrace() != null) {
      checkOpenCurlyBrace(
        tree.openCurlyBrace(),
        tree.closeCurlyBrace(),
        new TokenVisitor(tree).prevToken(tree.openCurlyBrace()));
    }
  }

  private void checkBlock(Tree body, SyntaxToken prevToken) {
    if (body.is(Kind.BLOCK)) {
      BlockTree blockTree = (BlockTree)body;
      checkOpenCurlyBrace(blockTree.openCurlyBraceToken(), blockTree.closeCurlyBraceToken(), prevToken);
    }
  }

  private void checkOpenCurlyBrace(SyntaxToken lBrace, SyntaxToken rBrace, SyntaxToken prevToken) {
    int leftBraceLine = lBrace.line();
    if (leftBraceLine != rBrace.line() && leftBraceLine == prevToken.line()) {
      context().newIssue(this, lBrace, MESSAGE);
    }
  }

  private static SyntaxToken getLastToken(Tree tree) {
    return ((PHPTree) tree).getLastToken();
  }

}
