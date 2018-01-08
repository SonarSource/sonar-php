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
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = OverridingMethodSimplyCallParentCheck.KEY)
public class OverridingMethodSimplyCallParentCheck extends PHPVisitorCheck {

  public static final String KEY = "S1185";
  private static final String MESSAGE = "Remove this method \"%s\" to simply inherit it.";

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);
    visitClass(tree);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    super.visitAnonymousClass(tree);
    visitClass(tree);
  }

  private void visitClass(ClassTree tree) {
    if (tree.superClass() != null) {
      String superClass = tree.superClass().fullName();
      for (ClassMemberTree member : tree.members()) {
        if (member.is(Kind.METHOD_DECLARATION)) {
          checkMethod((MethodDeclarationTree) member, superClass);
        }
      }
    }
  }

  private void checkMethod(MethodDeclarationTree method, String superClass) {
    if (method.body().is(Kind.BLOCK)) {
      BlockTree blockTree = (BlockTree)method.body();

      if (blockTree.statements().size() == 1) {
        StatementTree statementTree = blockTree.statements().get(0);

        ExpressionTree expressionTree = null;
        if (statementTree.is(Kind.EXPRESSION_STATEMENT)) {
          expressionTree = ((ExpressionStatementTree) statementTree).expression();

        } else if (statementTree.is(Kind.RETURN_STATEMENT)) {
          expressionTree = ((ReturnStatementTree) statementTree).expression();
        }

        checkExpression(expressionTree, method, superClass);
      }
    }
  }

  private void checkExpression(@Nullable ExpressionTree expressionTree, MethodDeclarationTree method, String superClass) {
    if (expressionTree != null && expressionTree.is(Kind.FUNCTION_CALL)) {
      FunctionCallTree functionCallTree = (FunctionCallTree)expressionTree;

      if (functionCallTree.callee().is(Kind.CLASS_MEMBER_ACCESS)) {
        MemberAccessTree memberAccessTree = (MemberAccessTree)functionCallTree.callee();

        String methodName = method.name().text();
        boolean sameMethodName = memberAccessTree.member().toString().equals(methodName);

        if (isSuperClass(memberAccessTree.object(), superClass) && sameMethodName && sameArguments(functionCallTree, method)) {
          String message = String.format(MESSAGE, methodName);
          context().newIssue(this, method.name(), message);
        }
      }
    }
  }

  private static boolean sameArguments(FunctionCallTree functionCallTree, MethodDeclarationTree method) {
    List<String> argumentNames = new ArrayList<>();
    for (ExpressionTree argument : functionCallTree.arguments()) {
      if (!argument.is(Kind.VARIABLE_IDENTIFIER)) {
        return false;
      }
      argumentNames.add(((VariableIdentifierTree) argument).variableExpression().text());
    }

    List<String> parameterNames = new ArrayList<>();
    for (ParameterTree parameter : method.parameters().parameters()) {
      parameterNames.add(parameter.variableIdentifier().variableExpression().text());
    }

    return argumentNames.equals(parameterNames);
  }

  private static boolean isSuperClass(ExpressionTree tree, String superClass) {
    String str = tree.toString();
    return superClass.equalsIgnoreCase(str) || "parent".equalsIgnoreCase(str);
  }

}
