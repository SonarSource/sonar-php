/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php.checks.phpunit;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.php.tree.TreeUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S5899")
public class NotDiscoverableTestCheck extends PhpUnitCheck {
  private static final String MESSAGE_VISIBLE = "Adjust the visibility of this test method so that it can be executed by the test runner.";
  private static final String MESSAGE_MARKED = "Mark this method as a test so that it can be executed by the test runner.";
  private static final Set<String> OVERRIDABLE_METHODS = SetUtils.immutableSetOf(
    "setup",
    "teardown",
    "setupbeforeclass",
    "teardownafterclass");
  private static final Set<String> SELF_OBJECTS = SetUtils.immutableSetOf("$this", "self", "static");

  private Map<String, Set<String>> internalCalledMethods = new HashMap<>();
  private Set<String> testMethods = new HashSet<>();

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    if (CheckUtils.isAbstract(tree) || CheckUtils.getClassName(tree).endsWith("TestCase")) {
      return;
    }

    super.visitClassDeclaration(tree);
  }

  @Override
  protected void visitPhpUnitTestCase(ClassDeclarationTree tree) {
    InternalCallsFindVisitor callsFindVisitor = new InternalCallsFindVisitor();
    tree.accept(callsFindVisitor);
    internalCalledMethods = callsFindVisitor.calledFunctions;
    testMethods = callsFindVisitor.testMethods;

    super.visitPhpUnitTestCase(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (!isPhpUnitTestCase()) {
      return;
    }

    if (!CheckUtils.isPublic(tree) && isMarkedAsTestMethod(tree)) {
      newIssue(tree.name(), MESSAGE_VISIBLE);
    } else if (CheckUtils.isPublic(tree) && !isMarkedAsTestMethod(tree)
               && !isCalledMethod(tree) && methodContainsAssertions(tree)) {
      newIssue(tree.name(), MESSAGE_MARKED);
    }
  }

  private boolean isCalledMethod(MethodDeclarationTree tree) {
    String methodName = tree.name().text().toLowerCase(Locale.ROOT);

    return testMethods.stream().anyMatch(t -> callPathExists(t, methodName));
  }

  // Perform a DFS to check if a path between two internal methods exists
  private boolean callPathExists(String start, String end) {
    Set<String> visited = new HashSet<>();
    Deque<String> stack = new ArrayDeque<>();

    stack.push(start);
    while (!stack.isEmpty()) {
      String currentEl = stack.pop();

      if (currentEl.equals(end)) {
        return true;
      }

      if (!visited.contains(currentEl) && internalCalledMethods.containsKey(currentEl)) {
        visited.add(currentEl);
        internalCalledMethods.get(currentEl).forEach(stack::push);
      }
    }

    return false;
  }

  private static boolean methodContainsAssertions(MethodDeclarationTree tree) {
    AssertionsFindVisitor assertionsFindVisitor = new AssertionsFindVisitor();
    tree.accept(assertionsFindVisitor);
    return assertionsFindVisitor.hasFoundAssertion;
  }

  private static boolean isMarkedAsTestMethod(MethodDeclarationTree tree) {
    return tree.name().text().startsWith("test") || CheckUtils.hasAnnotation(tree, "test");
  }

  private static class InternalCallsFindVisitor extends PhpUnitCheck {
    private final Map<String, Set<String>> calledFunctions = new HashMap<>();
    private final Set<String> testMethods = new HashSet<>();
    private String currentMethodName;

    @Override
    public void visitMethodDeclaration(MethodDeclarationTree tree) {
      currentMethodName = tree.name().text().toLowerCase(Locale.ROOT);
      if (OVERRIDABLE_METHODS.contains(currentMethodName)) {
        testMethods.add(currentMethodName);
      }
      super.visitMethodDeclaration(tree);
    }

    @Override
    protected void visitPhpUnitTestMethod(MethodDeclarationTree tree) {
      testMethods.add(currentMethodName);
    }

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      String functionName = CheckUtils.lowerCaseFunctionName(tree);
      if (functionName != null && isInternalMethodCall(tree)) {
        calledFunctions.computeIfAbsent(currentMethodName, f -> new HashSet<>()).add(functionName);
      }

      super.visitFunctionCall(tree);
    }

    private static boolean isInternalMethodCall(FunctionCallTree tree) {
      if (!tree.callee().is(Tree.Kind.OBJECT_MEMBER_ACCESS, Tree.Kind.CLASS_MEMBER_ACCESS)) {
        return false;
      }

      String objectString = ((MemberAccessTree) tree.callee()).object().toString().toLowerCase(Locale.ROOT);
      return SELF_OBJECTS.contains(objectString);
    }
  }

  private static class AssertionsFindVisitor extends PHPVisitorCheck {
    private boolean hasFoundAssertion = false;

    @Override
    public void visitFunctionExpression(FunctionExpressionTree tree) {
      // Do not visit nested anonymous functions
    }

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      if (isAssertion(tree)
        && TreeUtils.findAncestorWithKind(tree, Collections.singletonList(Tree.Kind.NEW_EXPRESSION)) == null) {
        hasFoundAssertion = true;
      }

      super.visitFunctionCall(tree);
    }
  }
}
