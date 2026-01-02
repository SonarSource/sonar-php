/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.MethodSymbol;
import org.sonar.php.symbols.Parameter;
import org.sonar.php.symbols.Symbol;
import org.sonar.php.tree.symbols.HasClassSymbol;
import org.sonar.php.tree.symbols.HasMethodSymbol;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
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

@Rule(key = "S1185")
public class OverridingMethodSimplyCallParentCheck extends PHPVisitorCheck {

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

  // existence of superclass is still checked
  @SuppressWarnings("java:S3655")
  private void visitClass(ClassTree tree) {
    if (tree.superClass() != null) {
      ClassSymbol superClassSymbol = ((HasClassSymbol) tree).symbol().superClass().get();
      for (ClassMemberTree member : tree.members()) {
        if (member.is(Kind.METHOD_DECLARATION)) {
          checkMethod((MethodDeclarationTree) member, superClassSymbol);
        }
      }
    }
  }

  private void checkMethod(MethodDeclarationTree method, ClassSymbol superClass) {
    if (method.body().is(Kind.BLOCK)) {
      BlockTree blockTree = (BlockTree) method.body();

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

  private void checkExpression(@Nullable ExpressionTree expressionTree, MethodDeclarationTree method, ClassSymbol superClass) {
    if (expressionTree != null && expressionTree.is(Kind.FUNCTION_CALL)) {
      FunctionCallTree functionCallTree = (FunctionCallTree) expressionTree;

      if (functionCallTree.callee().is(Kind.CLASS_MEMBER_ACCESS)) {
        MemberAccessTree memberAccessTree = (MemberAccessTree) functionCallTree.callee();
        String methodName = method.name().text();

        boolean isCallingSuperclassMethodWithSameNameAndArguments = isSuperClassReference(memberAccessTree.object(), superClass.qualifiedName().toString()) &&
          memberAccessTree.member().toString().equals(methodName) &&
          isFunctionCalledWithSameArgumentsAsDeclared(functionCallTree, method);
        if (isCallingSuperclassMethodWithSameNameAndArguments) {
          boolean duplicatesDeclarationFromSuper = Stream.iterate(superClass, Objects::nonNull, c -> c.superClass().orElse(null))
            .flatMap(c -> c.declaredMethods().stream())
            .anyMatch(ms -> ms.name().equalsIgnoreCase(methodName) &&
              hasSameVisibilityAs(method, ms) &&
              hasSameParameterList(method, ms));
          boolean isInheritanceChainUnresolvable = Stream.iterate(superClass, Objects::nonNull, c -> c.superClass().orElse(null))
            .anyMatch(Symbol::isUnknownSymbol);

          if (isInheritanceChainUnresolvable || duplicatesDeclarationFromSuper) {
            String message = String.format(MESSAGE, methodName);
            context().newIssue(this, method.name(), message);
          }
        }
      }
    }
  }

  private static boolean hasSameParameterList(MethodDeclarationTree method, MethodSymbol other) {
    var methodSymbol = ((HasMethodSymbol) method).symbol();
    List<Parameter> parameters = methodSymbol.parameters();
    List<Parameter> otherParameters = other.parameters();
    if (parameters.size() != otherParameters.size()) {
      return false;
    }
    for (int i = 0; i < parameters.size(); ++i) {
      if (!parameters.get(i).equals(otherParameters.get(i))) {
        return false;
      }
    }
    return true;
  }

  private static boolean hasSameVisibilityAs(MethodDeclarationTree method, MethodSymbol other) {
    return ((HasMethodSymbol) method).symbol().visibility() == other.visibility();
  }

  private static boolean isFunctionCalledWithSameArgumentsAsDeclared(FunctionCallTree functionCallTree, MethodDeclarationTree method) {
    List<String> argumentNames = new ArrayList<>();
    for (CallArgumentTree argument : functionCallTree.callArguments()) {
      if (!argument.value().is(Kind.VARIABLE_IDENTIFIER) || argument.name() != null) {
        return false;
      }
      argumentNames.add(((VariableIdentifierTree) argument.value()).variableExpression().text());
    }

    List<String> parameterNames = new ArrayList<>();
    for (ParameterTree parameter : method.parameters().parameters()) {
      if (parameter.initValue() != null) {
        return false;
      }
      parameterNames.add(parameter.variableIdentifier().variableExpression().text());
    }

    return argumentNames.equals(parameterNames);
  }

  private static boolean isSuperClassReference(ExpressionTree tree, String superClass) {
    String str = tree.toString();
    return superClass.equalsIgnoreCase(str) || "parent".equalsIgnoreCase(str);
  }

}
