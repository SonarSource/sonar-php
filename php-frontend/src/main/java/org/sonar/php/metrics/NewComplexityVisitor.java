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
package org.sonar.php.metrics;

import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.CaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.GotoStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.List;

public class NewComplexityVisitor extends PHPVisitorCheck {

  private int complexity = 0;

  @Override
  public void visitCaseClause(CaseClauseTree tree) {
    incrementComplexity();
    super.visitCaseClause(tree);
  }

  @Override
  public void visitWhileStatement(WhileStatementTree tree) {
    incrementComplexity();
    super.visitWhileStatement(tree);
  }

  @Override
  public void visitDoWhileStatement(DoWhileStatementTree tree) {
    incrementComplexity();
    super.visitDoWhileStatement(tree);
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    incrementComplexity();
    super.visitIfStatement(tree);
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    incrementComplexity();
    super.visitForStatement(tree);
  }

  @Override
  public void visitForEachStatement(ForEachStatementTree tree) {
    incrementComplexity();
    super.visitForEachStatement(tree);
  }

  @Override
  public void visitThrowStatement(ThrowStatementTree tree) {
    incrementComplexity();
    super.visitThrowStatement(tree);
  }

  @Override
  public void visitReturnStatement(ReturnStatementTree tree) {
    incrementComplexity();
    super.visitReturnStatement(tree);
  }

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    incrementComplexity();
    super.visitCatchBlock(tree);
  }

  @Override
  public void visitGotoStatement(GotoStatementTree tree) {
    incrementComplexity();
    super.visitGotoStatement(tree);
  }

  @Override
  public void visitConditionalExpression(ConditionalExpressionTree tree) {
    incrementComplexity();
    super.visitConditionalExpression(tree);
  }

  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {
    if (tree.is(
      Kind.CONDITIONAL_AND,
      Kind.CONDITIONAL_OR,
      Kind.ALTERNATIVE_CONDITIONAL_AND,
      Kind.ALTERNATIVE_CONDITIONAL_OR)) {
      incrementComplexity();
    }
    super.visitBinaryExpression(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    processFunction(tree);
    super.visitMethodDeclaration(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    processFunction(tree);
    super.visitFunctionDeclaration(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    processFunction(tree);
    super.visitFunctionExpression(tree);
  }

  private void processFunction(FunctionTree tree) {
    if (tree.is(Kind.FUNCTION_EXPRESSION) || !endsWithReturn(tree)) {
      incrementComplexity();
    }
  }

  private static boolean endsWithReturn(FunctionTree function) {
    Tree body = function.body();
    if (body.is(Kind.BLOCK)) {
      BlockTree block = (BlockTree) body;
      List<StatementTree> statements = block.statements();
      if (statements.isEmpty()) {
        return false;
      }
      return statements.get(statements.size() - 1).is(Kind.RETURN_STATEMENT);
    }
    return false;
  }

  private void incrementComplexity() {
    complexity++;
  }

  public static int complexity(Tree tree) {
    NewComplexityVisitor visitor = new NewComplexityVisitor();
    tree.accept(visitor);
    return visitor.complexity;
  }

  public static int complexityWithoutNestedFunctions(Tree tree) {
    NewComplexityVisitor visitor = new ShallowComplexityVisitor(tree);
    tree.accept(visitor);
    return visitor.complexity;
  }

  public static class ShallowComplexityVisitor extends NewComplexityVisitor {

    private Tree root;

    public ShallowComplexityVisitor(Tree root) {
      this.root = root;
    }

    @Override
    public void visitMethodDeclaration(MethodDeclarationTree tree) {
      if (tree.equals(root)) {
        super.visitMethodDeclaration(tree);
      }
    }

    @Override
    public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
      if (tree.equals(root)) {
        super.visitFunctionDeclaration(tree);
      }
    }

    @Override
    public void visitFunctionExpression(FunctionExpressionTree tree) {
      if (tree.equals(root)) {
        super.visitFunctionExpression(tree);
      }
    }

  }

}
