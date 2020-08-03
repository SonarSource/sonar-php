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
package org.sonar.php.checks.phpunit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.tree.TreeUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Rule(key = "S5899")
public class NotDiscoverableTestCheck extends PhpUnitCheck {
  private static final String MESSAGE_VISIBLE = "Adjust the visibility of this test method so that it can be executed by the test runner.";
  private static final String MESSAGE_MARKED = "Mark this method as a test so that it can be executed by the test runner.";
  private static final Set<String> OVERRIDABLE_METHODS = ImmutableSet.of(
    "setup",
    "teardown",
    "setupbeforeclass",
    "teardownafterclass");
  private static final ImmutableSet<String> SELF_OBJECTS = ImmutableSet.of("$this", "self", "static");

  private Set<String> internalCalledMethods = new HashSet<>();

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

    super.visitPhpUnitTestCase(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (!isPhpUnitTestCase()) {
      return;
    }

    if (!CheckUtils.isPublic(tree) && isMarkedAsTestMethod(tree)) {
      context().newIssue(this, tree.name(), MESSAGE_VISIBLE);
    } else if (CheckUtils.isPublic(tree)
      && !isMarkedAsTestMethod(tree)
      && !internalCalledMethods.contains(tree.name().text().toLowerCase(Locale.ROOT))
      && !OVERRIDABLE_METHODS.contains(tree.name().text().toLowerCase(Locale.ROOT))
      && methodContainsAssertions(tree)) {
      context().newIssue(this, tree.name(), MESSAGE_MARKED);
    }
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
    private final Set<String> calledFunctions = new HashSet<>();

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      if (!isPhpUnitTestMethod()) {
        return;
      }

      String functionName = getFunctionName(tree);
      if (functionName != null && isInternalMethodCall(tree)) {
        calledFunctions.add(functionName);
      }

      super.visitFunctionCall(tree);
    }

    private static String getFunctionName(FunctionCallTree tree) {
      String functionName = CheckUtils.getLowerCaseFunctionName(tree);

      if (functionName != null && functionName.contains("::")) {
        functionName = functionName.substring(functionName.lastIndexOf("::") + 2);
      }

      return functionName;
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
        && TreeUtils.findAncestorWithKind(tree, ImmutableList.of(Tree.Kind.NEW_EXPRESSION)) == null) {
        hasFoundAssertion = true;
      }

      super.visitFunctionCall(tree);
    }
  }
}
