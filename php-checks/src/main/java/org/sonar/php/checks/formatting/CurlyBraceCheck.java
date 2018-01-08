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
package org.sonar.php.checks.formatting;

import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.FormattingStandardCheck;
import org.sonar.php.checks.utils.TokenVisitor;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import javax.annotation.Nullable;
import java.util.List;

public class CurlyBraceCheck extends PHPVisitorCheck implements FormattingCheck {

  private static final String DECLARATIONS_OPEN_CURLY_MESSAGE = "Move this open curly brace to the beginning of the next line.";
  private static final String CONTROL_STRUCTURES_OPEN_CURLY_MESSAGE = "Move this open curly brace to the end of the previous line.";
  private static final String KEYWORD_MESSAGE = "Move this \"%s\" to the same line as the previous closing curly brace.";

  private FormattingStandardCheck check = null;

  @Override
  public void checkFormat(FormattingStandardCheck formattingCheck, ScriptTree scriptTree) {
    this.check = formattingCheck;
    super.visitScript(scriptTree);
  }

  /**
   * Class & function declarations open curly brace
   */

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    SyntaxToken previousToken = new TokenVisitor(tree).prevToken(tree.openCurlyBraceToken());
    checkDeclarationOpenCurly(previousToken, tree.openCurlyBraceToken());

    super.visitClassDeclaration(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (tree.body().is(Tree.Kind.BLOCK)) {
      checkDeclarationOpenCurly(getPreviousToken(tree.parameters()), ((BlockTree) tree.body()).openCurlyBraceToken());
    }

    super.visitMethodDeclaration(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    checkDeclarationOpenCurly(getPreviousToken(tree.parameters()), tree.body().openCurlyBraceToken());
    super.visitFunctionDeclaration(tree);
  }

  /**
   *
   * Returns token previous to closing parenthesis token.
   * It is required to cover case (which should not raise issues):
   * <code>
   *  function g($p1, $p2,
   *           $p3, $p4
   *  ) {
   *  }
   * </code>
   */
  private static SyntaxToken getPreviousToken(ParameterListTree parameterList) {
    return new TokenVisitor(parameterList).prevToken(parameterList.closeParenthesisToken());
  }

  /**
   * Control structures open curly brace
   */

  @Override
  public void visitElseifClause(ElseifClauseTree tree) {
    checkControlStructureOpenCurly(tree.condition().closeParenthesis(), getOpenCurlyIfBlock(tree.statements()));
    super.visitElseifClause(tree);
  }


  @Override
  public void visitIfStatement(IfStatementTree tree) {
    checkControlStructureOpenCurly(tree.condition().closeParenthesis(), getOpenCurlyIfBlock(tree.statements()));

    // Check else keyword
    ElseClauseTree elseClause = tree.elseClause();
    if (elseClause != null) {
      checkCloseCurlyNextToKeyword(new TokenVisitor(tree).prevToken(elseClause.elseToken()), elseClause.elseToken());
    }
    tree.elseifClauses().stream()
      .map(ElseifClauseTree::elseifToken)
      .forEach(elseIfClause -> checkCloseCurlyNextToKeyword(new TokenVisitor(tree).prevToken(elseIfClause), elseIfClause));
    super.visitIfStatement(tree);
  }

  @Override
  public void visitElseClause(ElseClauseTree tree) {
    checkControlStructureOpenCurly(tree.elseToken(), getOpenCurlyIfBlock(tree.statements()));
    super.visitElseClause(tree);
  }

  @Override
  public void visitWhileStatement(WhileStatementTree tree) {
    checkControlStructureOpenCurly(tree.condition().closeParenthesis(), getOpenCurlyIfBlock(tree.statements()));
    super.visitWhileStatement(tree);
  }

  @Override
  public void visitDoWhileStatement(DoWhileStatementTree tree) {
    if (tree.statement().is(Tree.Kind.BLOCK)) {
      checkControlStructureOpenCurly(tree.doToken(), ((BlockTree) tree.statement()).openCurlyBraceToken());
    }
    super.visitDoWhileStatement(tree);
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    checkControlStructureOpenCurly(tree.closeParenthesisToken(), getOpenCurlyIfBlock(tree.statements()));
    super.visitForStatement(tree);
  }

  @Override
  public void visitForEachStatement(ForEachStatementTree tree) {
    checkControlStructureOpenCurly(tree.closeParenthesisToken(), getOpenCurlyIfBlock(tree.statements()));
    super.visitForEachStatement(tree);
  }

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    checkControlStructureOpenCurly(tree.closeParenthesisToken(), tree.block().openCurlyBraceToken());
    super.visitCatchBlock(tree);
  }

  @Override
  public void visitTryStatement(TryStatementTree tree) {
    checkControlStructureOpenCurly(tree.tryToken(), tree.block().openCurlyBraceToken());
    TokenVisitor tokenVisitor = new TokenVisitor(tree);

    // Check catch keyword
    for (CatchBlockTree catchBlock : tree.catchBlocks()) {
      checkCloseCurlyNextToKeyword(tokenVisitor.prevToken(catchBlock.catchToken()), catchBlock.catchToken());
    }

    // Check finally keyword
    if (tree.finallyBlock() != null) {
      checkCloseCurlyNextToKeyword(tokenVisitor.prevToken(tree.finallyToken()), tree.finallyToken());
    }
    super.visitTryStatement(tree);
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    checkControlStructureOpenCurly(tree.expression().closeParenthesis(), tree.openCurlyBraceToken());
    super.visitSwitchStatement(tree);
  }

  private void checkDeclarationOpenCurly(@Nullable SyntaxToken previousToken, @Nullable SyntaxToken openCurly) {
    if (!check.isOpenCurlyBraceForClassAndFunction || previousToken == null || openCurly == null) {
      return;
    }
    if (TokenUtils.isOnSameLine(previousToken, openCurly)) {
      reportIssue(openCurly, DECLARATIONS_OPEN_CURLY_MESSAGE);
    }
  }

  private void checkControlStructureOpenCurly(@Nullable SyntaxToken previousToken, @Nullable SyntaxToken openCurly) {
    if (!check.isOpenCurlyBraceForControlStructures || previousToken == null || openCurly == null) {
      return;
    }
    if (!TokenUtils.isOnSameLine(previousToken, openCurly)) {
      reportIssue(openCurly, CONTROL_STRUCTURES_OPEN_CURLY_MESSAGE);
    }
  }

  private void checkCloseCurlyNextToKeyword(@Nullable SyntaxToken previousToken, @Nullable SyntaxToken keyword) {
    if (!check.isClosingCurlyNextToKeyword || previousToken == null || keyword == null) {
      return;
    }
    if (isCloseCurly(previousToken) && !TokenUtils.isOnSameLine(previousToken, keyword)) {
      reportIssue(keyword, String.format(KEYWORD_MESSAGE, keyword.text()));
    }
  }

  private static boolean isCloseCurly(SyntaxToken token) {
    return PHPPunctuator.RCURLYBRACE.getValue().equals(token.text());
  }

  @Nullable
  private static SyntaxToken getOpenCurlyIfBlock(List<StatementTree> statements) {
    if (!statements.isEmpty()) {
      StatementTree firstStmt = statements.get(0);
      return firstStmt.is(Tree.Kind.BLOCK) ? ((BlockTree) firstStmt).openCurlyBraceToken() : null;
    }
    return null;
  }

  private void reportIssue(Tree tree, String message) {
    check.reportIssue(message, tree);
  }

}
