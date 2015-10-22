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
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Rule(
  key = OverridingMethodSimplyCallParentCheck.KEY,
  name = "Overriding methods should do more than simply call the same method in the super class",
  priority = Priority.MINOR,
  tags = Tags.CLUMSY)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("5min")
public class OverridingMethodSimplyCallParentCheck extends PHPVisitorCheck {

  public static final String KEY = "S1185";
  private static final String MESSAGE = "Remove this method \"%s\" to simply inherit it.";

  private String superClass = null;

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);

    if (tree.superClass() != null) {
      superClass = tree.superClass().fullName();

      for (ClassMemberTree member : tree.members()) {
        if (member.is(Kind.METHOD_DECLARATION)) {
          checkMethod((MethodDeclarationTree) member);
        }
      }

      superClass = null;
    }
  }

  private void checkMethod(MethodDeclarationTree method) {
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

        checkExpression(expressionTree, method);
      }
    }
  }

  private void checkExpression(@Nullable ExpressionTree expressionTree, MethodDeclarationTree method) {
    if (expressionTree != null && expressionTree.is(Kind.FUNCTION_CALL)) {
      FunctionCallTree functionCallTree = (FunctionCallTree)expressionTree;

      if (functionCallTree.callee().is(Kind.CLASS_MEMBER_ACCESS)) {
        MemberAccessTree memberAccessTree = (MemberAccessTree)functionCallTree.callee();

        String methodName = method.name().text();
        boolean sameMethodName = CheckUtils.asString(memberAccessTree.member()).equals(methodName);

        if (isSuperClass(memberAccessTree.object()) && sameMethodName && sameArguments(functionCallTree, method)) {
          String message = String.format(MESSAGE, methodName);
          context().newIssue(KEY, message).tree(method);
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

  private boolean isSuperClass(ExpressionTree tree) {
    String str = CheckUtils.asString(tree);
    return superClass.equalsIgnoreCase(str) || "parent".equalsIgnoreCase(str);
  }

}
