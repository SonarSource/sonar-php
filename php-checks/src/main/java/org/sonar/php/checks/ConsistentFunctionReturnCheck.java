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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = ConsistentFunctionReturnCheck.KEY)
public class ConsistentFunctionReturnCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S3801";

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Arrays.asList(Tree.Kind.FUNCTION_DECLARATION, Tree.Kind.METHOD_DECLARATION, Tree.Kind.FUNCTION_EXPRESSION);
  }

  @Override
  public void visitNode(Tree tree) {
    FunctionTree functionTree = (FunctionTree) tree;
    List<ReturnStatementTree> returnStatements = getReturnedStatements(functionTree.body());
    int numberReturn = returnStatements.size();
    if (numberReturn > 1) {
      long numberEmptyReturn = returnStatements.stream().map(ReturnStatementTree::expression).filter(Objects::isNull).count();
      if (numberEmptyReturn > 0 && numberEmptyReturn != numberReturn) {
        PreciseIssue issue = context().newIssue(this, functionName(tree), "Refactor this function to use \"return\" consistently.");
        returnStatements.forEach(returnStatement -> issue.secondary(returnStatement, ""));
      }
    }
  }

  private static Tree functionName(Tree tree) {
    if (tree.is(Tree.Kind.FUNCTION_DECLARATION)) {
      return ((FunctionDeclarationTree) tree).name();
    }
    if (tree.is(Tree.Kind.METHOD_DECLARATION)) {
      return ((MethodDeclarationTree) tree).name();
    }
    return ((FunctionExpressionTree) tree).functionToken();
  }

  private static List<ReturnStatementTree> getReturnedStatements(Tree body) {
    ReturnStatementCollector visitor = new ReturnStatementCollector();
    body.accept(visitor);
    return visitor.returnStatements;
  }

  private static class ReturnStatementCollector extends PHPVisitorCheck {
    List<ReturnStatementTree> returnStatements = new ArrayList<>();

    @Override
    public void visitReturnStatement(ReturnStatementTree tree) {
      returnStatements.add(tree);
      super.visitReturnStatement(tree);
    }

    @Override
    public void visitClassDeclaration(ClassDeclarationTree tree) {
      // skip classes
    }

    @Override
    public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
      // skip other functions
    }

    @Override
    public void visitFunctionExpression(FunctionExpressionTree tree) {
      // skip other functions
    }
  }

}
