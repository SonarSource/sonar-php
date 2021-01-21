/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
package org.sonar.php.checks.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
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

import static java.util.Collections.singletonList;
import static org.sonar.php.checks.utils.CheckUtils.argument;
import static org.sonar.php.checks.utils.CheckUtils.getFunctionName;
import static org.sonar.php.checks.utils.CheckUtils.hasModifier;
import static org.sonar.php.checks.utils.CheckUtils.nameOf;
import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;
import static org.sonar.php.symbols.Symbols.get;
import static org.sonar.php.tree.TreeUtils.descendants;
import static org.sonar.php.tree.TreeUtils.findAncestorWithKind;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;
import static org.sonar.plugins.php.api.tree.Tree.Kind.BOOLEAN_LITERAL;
import static org.sonar.plugins.php.api.tree.Tree.Kind.CLASS_MEMBER_ACCESS;
import static org.sonar.plugins.php.api.tree.Tree.Kind.FUNCTION_CALL;
import static org.sonar.plugins.php.api.tree.Tree.Kind.FUNCTION_EXPRESSION;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NULL_LITERAL;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NUMERIC_LITERAL;
import static org.sonar.plugins.php.api.tree.Tree.Kind.REGULAR_STRING_LITERAL;
import static org.sonar.plugins.php.api.tree.Tree.Kind.VARIABLE_IDENTIFIER;

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
        checkReturnStatements(methodDeclarationTree, a -> (VOTER_INTERFACE_COMPLIANT_RETURN_VALUES.contains(a)));
      }
      if ("voteOnAttribute".equalsIgnoreCase(functionName) && isMethodInheritedFromClassOrInterface(SYMFONY_VOTER_NAMESPACE, methodDeclarationTree)) {
        checkReturnStatements(methodDeclarationTree, a -> ("false".equals(a)));
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
      if (argument.isPresent()) {
        ExpressionTree callArgumentTree = argument.get().value();
        if (callArgumentTree.is(FUNCTION_EXPRESSION)) {
          checkReturnStatements((FunctionExpressionTree) callArgumentTree, LARAVEL_GATE_CLOSURE_COMPLIANT_RETURN_VALUES::contains);
        }
      }
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

  private static boolean isMethodInheritedFromClassOrInterface(QualifiedName qualifiedName, MethodDeclarationTree methodDeclarationTree) {
    ClassDeclarationTree classTree = (ClassDeclarationTree) findAncestorWithKind(methodDeclarationTree, singletonList(Kind.CLASS_DECLARATION));
    if (classTree != null) {
      return get(classTree).isSubTypeOf(qualifiedName).isTrue();
    }
    return false;
  }

  private void checkReturnStatements(FunctionTree methodDeclarationTree, Predicate<String> predicate) {
    List<ReturnStatementTree> returnStatements = descendants(methodDeclarationTree, ReturnStatementTree.class).collect(Collectors.toList());
    for (ReturnStatementTree returnStatementTree : returnStatements) {
      if (CompliantResultStatement.isReturnStatementCompliant(returnStatementTree, predicate)) {
        return;
      }
    }
    if (returnStatements.isEmpty()) {
      context().newIssue(this, methodDeclarationTree, MESSAGE);
    } else {
      context().newIssue(this, returnStatements.get(returnStatements.size() - 1), MESSAGE);
    }
  }

  private static class CompliantResultStatement {

    private static boolean isReturnStatementCompliant(ReturnStatementTree returnStatementTree, Predicate<String> predicate) {
      return isExpressionCompliant(returnStatementTree.expression(), predicate);
    }

    private static boolean isExpressionCompliant(ExpressionTree returnExpressionTree, Predicate<String> predicate) {
      boolean compliant;
      if (returnExpressionTree.is(NUMERIC_LITERAL, REGULAR_STRING_LITERAL)) {
        compliant = false;
      } else if (returnExpressionTree.is(FUNCTION_CALL)) {
        compliant = isFunctionCallCompliant((FunctionCallTree) returnExpressionTree);
      } else if (returnExpressionTree.is(VARIABLE_IDENTIFIER)) {
        compliant = isVariableValueCompliant((VariableIdentifierTree) returnExpressionTree, predicate);
      } else if (returnExpressionTree.is(CLASS_MEMBER_ACCESS)) {
        compliant = isMemberValueCompliant((MemberAccessTree) returnExpressionTree, predicate);
      } else if (returnExpressionTree.is(NULL_LITERAL)) {
        compliant = isBooleanOrNullLiteralValueCompliant((LiteralTree) returnExpressionTree, predicate);
      } else if (returnExpressionTree.is(BOOLEAN_LITERAL)) {
        compliant = isBooleanOrNullLiteralValueCompliant((LiteralTree) returnExpressionTree, predicate);
      } else {
        compliant = true;
      }
      return compliant;
    }

    private static boolean isFunctionCallCompliant(FunctionCallTree functionCallTree) {
      return !nameOf(functionCallTree.callee()).equalsIgnoreCase("response::allow");
    }

    private static boolean isVariableValueCompliant(VariableIdentifierTree variableExpression, Predicate<String> predicate) {
      Optional<ExpressionTree> uniqueAssignedValue = CheckUtils.uniqueAssignedValue(variableExpression);
      if (uniqueAssignedValue.isPresent()) {
        return isExpressionCompliant(uniqueAssignedValue.get(), predicate);
      }
      return true;
    }

    private static boolean isBooleanOrNullLiteralValueCompliant(LiteralTree literalValue, Predicate<String> predicate) {
      return predicate.test(literalValue.value().toLowerCase(Locale.ROOT));
    }

    private static boolean isMemberValueCompliant(MemberAccessTree memberAccessTree, Predicate<String> predicate) {
      return predicate.test(nameOf(memberAccessTree.member()));
    }
  }
}
