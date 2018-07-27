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

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LexicalVariablesTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S836")
public class UseOfUninitializedVariableCheck extends PHPVisitorCheck {

  private static final Set<Kind> PARENT_INITIALIZATION_KIND = EnumSet.of(
    // Note: LEXICAL_VARIABLES are both, read and write, see VariableVisitor#visitFunctionExpression
    Kind.PARAMETER,
    Kind.GLOBAL_STATEMENT,
    Kind.VARIABLE_DECLARATION,
    Kind.REFERENCE_VARIABLE,
    Kind.ARRAY_ASSIGNMENT_PATTERN_ELEMENT,
    Kind.UNSET_VARIABLE_STATEMENT,
    // CatchBlockTree#variable
    Kind.CATCH_BLOCK,
    Kind.ASSIGNMENT_BY_REFERENCE);

  private static final Set<String> FUNCTION_CHANGING_CURRENT_SCOPE = new HashSet<>(Arrays.asList(
    "eval",
    "extract",
    "parse_str",
    // PREG_REPLACE_EVAL option was deprecated in php 5.5 and has been removed in php 7.0
    "preg_replace",
    "include",
    "include_once",
    "require",
    "require_once"));

  // Note: "$argc" and "$argv" are not available in the function scope without using "global"
  private static final Set<String> PREDEFINED_VARIABLES = new HashSet<>(Arrays.asList(
    "$_COOKIE",
    "$_ENV",
    "$_FILES",
    "$_GET",
    "$_POST",
    "$_REQUEST",
    "$_SERVER",
    "$_SESSION",
    "$GLOBALS",
    "$HTTP_RAW_POST_DATA",
    "$http_response_header",
    "$php_errormsg",
    // "$this" is defined only in method, but rule S2014 raises issues when it's used elsewhere
    "$this"));

  private static final Set<String> FUNCTION_ALLOWING_ARGUMENT_CHECK;
  static {
    FUNCTION_ALLOWING_ARGUMENT_CHECK = new HashSet<>(IgnoredReturnValueCheck.PURE_FUNCTIONS);
    FUNCTION_ALLOWING_ARGUMENT_CHECK.remove("isset");
  }

  private static final Map<Kind, Predicate<Tree>> IS_READ_ACCESS_BY_PARENT_KIND = initializeReadPredicate();

  private static Map<Kind, Predicate<Tree>> initializeReadPredicate() {
    Map<Kind, Predicate<Tree>> map = new EnumMap<>(Kind.class);

    PARENT_INITIALIZATION_KIND.forEach(kind -> map.put(kind, tree -> false));
    map.put(Kind.ASSIGNMENT, tree -> tree == ((AssignmentExpressionTree) tree.getParent()).value());
    map.put(Kind.FUNCTION_CALL, tree -> {
      FunctionCallTree functionCall = (FunctionCallTree) tree.getParent();
      return tree == functionCall.callee() || FUNCTION_ALLOWING_ARGUMENT_CHECK.contains(lowerCaseFunctionName(functionCall));
    });
    map.put(Kind.FOREACH_STATEMENT, UseOfUninitializedVariableCheck::isInsideForEachExpression);
    map.put(Kind.ALTERNATIVE_FOREACH_STATEMENT, UseOfUninitializedVariableCheck::isInsideForEachExpression);
    map.put(Kind.ARRAY_ACCESS, tree -> !isArrayAssignment(tree));
    map.put(Kind.PARENTHESISED_EXPRESSION, tree -> isReadAccess(tree.getParent()));

    return map;
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    checkFunction(tree);
    super.visitFunctionDeclaration(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    checkFunction(tree);
    super.visitFunctionExpression(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    checkFunction(tree);
    super.visitMethodDeclaration(tree);
  }

  private void checkFunction(FunctionTree function) {
    VariableVisitor visitor = new VariableVisitor(function);
    function.accept(visitor);
    visitor.uninitializedStream()
      .forEach(variable -> context().newIssue(this, variable, "Review the data-flow - use of uninitialized value."));
  }

  private static class VariableVisitor extends PHPVisitorCheck {

    final FunctionTree currentFunction;

    boolean trustVariables = true;

    Map<String, VariableIdentifierTree> firstVariableReadAccess = new HashMap<>();

    Set<String> initializedVariables = new HashSet<>();

    VariableVisitor(FunctionTree currentFunction) {
      this.currentFunction = currentFunction;
    }

    Stream<VariableIdentifierTree> uninitializedStream() {
      if (!trustVariables) {
        return Stream.empty();
      }
      return firstVariableReadAccess.entrySet().stream()
        .filter(entry -> !initializedVariables.contains(entry.getKey()))
        .map(Map.Entry::getValue);
    }

    @Override
    public void visitVariableIdentifier(VariableIdentifierTree tree) {
      if (isClassMemberAccess(tree) || uninitializedVariableDeclaration(tree)) {
        return;
      }
      String name = tree.text();
      if (!PREDEFINED_VARIABLES.contains(name)) {
        if (isReadAccess(tree)) {
          if (!firstVariableReadAccess.containsKey(name)) {
            firstVariableReadAccess.put(name, tree);
          }
        } else {
          initializedVariables.add(name);
        }
      }
    }

    @Override
    public void visitFunctionCall(FunctionCallTree functionCall) {
      if (FUNCTION_CHANGING_CURRENT_SCOPE.contains(lowerCaseFunctionName(functionCall))) {
        trustVariables = false;
      }
      super.visitFunctionCall(functionCall);
    }

    @Override
    public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
      if (tree == currentFunction) {
        super.visitFunctionDeclaration(tree);
      }
      // else skip nested
    }

    @Override
    public void visitFunctionExpression(FunctionExpressionTree tree) {
      LexicalVariablesTree lexicalVars = tree.lexicalVars();
      if (tree == currentFunction) {
        if (lexicalVars != null) {
          lexicalVars.variables().stream()
            .map(VariableTree::variableExpression)
            .filter(variable -> variable.is(Kind.VARIABLE_IDENTIFIER))
            .map(variable -> ((VariableIdentifierTree) variable).text())
            .forEach(initializedVariables::add);
        }
        super.visitFunctionExpression(tree);
      } else {
        if (lexicalVars != null) {
          scan(lexicalVars.variables());
        }
        // skip nested
      }
    }

    @Override
    public void visitClassDeclaration(ClassDeclarationTree tree) {
      // skip nested
    }

    @Override
    public void visitAnonymousClass(AnonymousClassTree tree) {
      // skip nested
    }

    private static boolean isClassMemberAccess(Tree tree) {
      Tree child = skipParentArrayAccess(tree);
      return (child.getParent().is(Kind.CLASS_MEMBER_ACCESS) &&
        ((MemberAccessTree) child.getParent()).member() == child);
    }

    private static boolean uninitializedVariableDeclaration(Tree tree) {
      return (tree.getParent().is(Kind.VARIABLE_DECLARATION) &&
        ((VariableDeclarationTree) tree.getParent()).equalToken() == null);
    }

  }

  private static boolean isReadAccess(Tree tree) {
    Predicate<Tree> predicate = IS_READ_ACCESS_BY_PARENT_KIND.get(tree.getParent().getKind());
    return predicate == null || predicate.test(tree);
  }

  @Nullable
  private static String lowerCaseFunctionName(FunctionCallTree functionCall) {
    String name = CheckUtils.getFunctionName(functionCall);
    return name != null ? name.toLowerCase(Locale.ROOT) : null;
  }

  private static boolean isArrayAssignment(Tree tree) {
    Tree child = skipParentArrayAccess(tree);
    return child.getParent().is(Kind.ASSIGNMENT) &&
      ((AssignmentExpressionTree) child.getParent()).variable() == child;
  }

  private static boolean isInsideForEachExpression(Tree tree) {
    return tree == ((ForEachStatementTree) tree.getParent()).expression();
  }

  private static Tree skipParentArrayAccess(Tree tree) {
    Tree child = tree;
    while (child.getParent().is(Kind.ARRAY_ACCESS) && ((ArrayAccessTree) child.getParent()).object() == child) {
      child = child.getParent();
    }
    return child;
  }

}
