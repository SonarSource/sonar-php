/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.argument;
import static org.sonar.php.checks.utils.CheckUtils.getFunctionName;
import static org.sonar.php.checks.utils.CheckUtils.hasModifier;
import static org.sonar.php.checks.utils.CheckUtils.isMethodInheritedFromClassOrInterface;
import static org.sonar.php.checks.utils.CheckUtils.nameOf;
import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;
import static org.sonar.php.tree.TreeUtils.descendants;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;
import static org.sonar.plugins.php.api.tree.Tree.Kind.CLASS_MEMBER_ACCESS;

@Rule(key = "S5808")
public class AuthorizationsCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Vote methods should return at least once a negative response";

  private static final QualifiedName SYMFONY_VOTER_INTERFACE_NAMESPACE = qualifiedName("Symfony\\Component\\Security\\Core\\Authorization\\Voter\\VoterInterface");
  private static final QualifiedName SYMFONY_VOTER_NAMESPACE = qualifiedName("Symfony\\Component\\Security\\Core\\Authorization\\Voter\\Voter");
  private static final QualifiedName LARAVEL_GATE_NAMESPACE = qualifiedName("Illuminate\\Support\\Facades\\Gate");

  private static final Set<String> VOTER_INTERFACE_COMPLIANT_RETURN_VALUES = new HashSet<>(Arrays.asList("ACCESS_ABSTAIN", "ACCESS_DENIED"));
  private static final Set<String> LARAVEL_GATE_CLOSURE_COMPLIANT_RETURN_VALUES = new HashSet<>(Arrays.asList("false", "null"));

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree methodDeclarationTree) {
    if (!hasModifier(methodDeclarationTree, "abstract")) {
      String functionName = trimQuotes(getFunctionName(methodDeclarationTree));
      if ("vote".equalsIgnoreCase(functionName) && isMethodInheritedFromClassOrInterface(SYMFONY_VOTER_INTERFACE_NAMESPACE, methodDeclarationTree)) {
        checkReturnStatements(methodDeclarationTree, VOTER_INTERFACE_COMPLIANT_RETURN_VALUES::contains);
      }
      if ("voteOnAttribute".equalsIgnoreCase(functionName) && isMethodInheritedFromClassOrInterface(SYMFONY_VOTER_NAMESPACE, methodDeclarationTree)) {
        checkReturnStatements(methodDeclarationTree, "false"::equals);
      }
    }
    super.visitMethodDeclaration(methodDeclarationTree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();
    if (callee.is(CLASS_MEMBER_ACCESS)) {
      MemberAccessTree memberAccessTree = (MemberAccessTree) callee;
      Optional<CallArgumentTree> argument = Optional.empty();
      if (isLaravelGateMethod(memberAccessTree, "define")) {
        argument = argument(tree, "callback", 1);
      }
      if (isLaravelGateMethod(memberAccessTree, "before") || isLaravelGateMethod(memberAccessTree, "after")) {
        argument = argument(tree, "callback", 0);
      }
      argument
        .map(CallArgumentTree::value)
        .filter(FunctionExpressionTree.class::isInstance)
        .map(FunctionExpressionTree.class::cast)
        .ifPresent(a -> checkReturnStatements(a, LARAVEL_GATE_CLOSURE_COMPLIANT_RETURN_VALUES::contains));
    }
    super.visitFunctionCall(tree);
  }

  private boolean isLaravelGateMethod(MemberAccessTree memberAccessTree, String expectedMethod) {
    ExpressionTree receiver = memberAccessTree.object();
    Tree method = memberAccessTree.member();
    return method.is(Kind.NAME_IDENTIFIER) &&
      ((NameIdentifierTree) method).text().equals(expectedMethod) &&
      receiver.is(Kind.NAMESPACE_NAME) &&
      getFullyQualifiedName((NamespaceNameTree) receiver).equals(LARAVEL_GATE_NAMESPACE);
  }

  private void checkReturnStatements(FunctionTree methodDeclarationTree, Predicate<String> predicate) {
    List<ReturnStatementTree> returnStatements = descendants(methodDeclarationTree, ReturnStatementTree.class).toList();
    for (ReturnStatementTree returnStatementTree : returnStatements) {
      if (CompliantResultStatement.create(returnStatementTree.expression(), predicate).isCompliant()) {
        return;
      }
    }
    if (returnStatements.isEmpty()) {
      context().newIssue(this, methodDeclarationTree, MESSAGE);
    } else {
      context().newIssue(this, returnStatements.get(returnStatements.size() - 1), MESSAGE);
    }
  }

  private record CompliantResultStatement(ExpressionTree returnExpressionTree, Predicate<String> predicate) {

    static CompliantResultStatement create(ExpressionTree returnExpressionTree, Predicate<String> predicate) {
      return new CompliantResultStatement(returnExpressionTree, predicate);
    }

    boolean isCompliant() {
      return switch (returnExpressionTree.getKind()) {
        case NUMERIC_LITERAL, REGULAR_STRING_LITERAL -> false;
        case FUNCTION_CALL -> isFunctionCallCompliant();
        case VARIABLE_IDENTIFIER -> isVariableValueCompliant();
        case CLASS_MEMBER_ACCESS -> isMemberValueCompliant();
        case NULL_LITERAL, BOOLEAN_LITERAL -> isBooleanOrNullLiteralValueCompliant();
        default -> true;
      };
    }

    boolean isFunctionCallCompliant() {
      return !"response::allow".equalsIgnoreCase(nameOf(((FunctionCallTree) returnExpressionTree).callee()));
    }

    boolean isVariableValueCompliant() {
      Optional<ExpressionTree> uniqueAssignedValue = CheckUtils.uniqueAssignedValue((VariableIdentifierTree) returnExpressionTree);
      return uniqueAssignedValue.map(expressionTree -> create(expressionTree, predicate).isCompliant()).orElse(true);
    }

    boolean isBooleanOrNullLiteralValueCompliant() {
      return predicate.test(((LiteralTree) returnExpressionTree).value().toLowerCase(Locale.ROOT));
    }

    boolean isMemberValueCompliant() {
      return predicate.test(nameOf(((MemberAccessTree) returnExpressionTree).member()));
    }
  }
}
