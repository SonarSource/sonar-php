/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import java.util.Iterator;
import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.tree.TreeUtils;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;

import static org.sonar.php.checks.utils.CheckUtils.assignedValue;
import static org.sonar.php.checks.utils.CheckUtils.hasNamedArgument;
import static org.sonar.plugins.php.api.tree.Tree.Kind;

@Rule(key = "S3415")
public class AssertionArgumentOrderCheck extends PhpUnitCheck {

  private static final String MESSAGE = "Swap these 2 arguments so they are in the correct order: expected value, actual value.";
  private static final String SECONDARY_MESSAGE = "Other argument to swap.";
  private static final Kind[] LITERAL = {Kind.BOOLEAN_LITERAL, Kind.NULL_LITERAL, Kind.NUMERIC_LITERAL, Kind.EXPANDABLE_STRING_LITERAL,
    Kind.REGULAR_STRING_LITERAL};

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (!isPhpUnitTestMethod()) {
      return;
    }

    Optional<Assertion> assertion = getAssertion(tree);
    SeparatedList<CallArgumentTree> arguments = tree.callArguments();
    if (arguments.size() >= 2 && assertion.isPresent() && assertion.get().hasExpectedValue() && !hasNamedArgument(tree)) {
      ExpressionTree expected = arguments.get(0).value();
      ExpressionTree actual = arguments.get(1).value();
      if (isLiteralOrClassNameOrParameter(actual) && !isLiteralOrClassNameOrParameter(expected)) {
        newIssue(actual, MESSAGE).secondary(expected, SECONDARY_MESSAGE);
      }
    }

    super.visitFunctionCall(tree);
  }

  private static boolean isLiteralOrClassNameOrParameter(ExpressionTree expression) {
    return assignedValue(expression).is(LITERAL) ||
      isStaticAccessWithName(expression, "class") ||
      isDefinedAsParameter(expression);
  }

  private static boolean isStaticAccessWithName(ExpressionTree expression, String memberName) {
    if (expression instanceof MemberAccessTree) {
      MemberAccessTree tree = (MemberAccessTree) expression;
      return tree.isStatic() && memberName.equals(name(tree.member()));
    }
    return false;
  }

  private static boolean isDefinedAsParameter(ExpressionTree expression) {
    MethodDeclarationTree method = (MethodDeclarationTree) TreeUtils.findAncestorWithKind(expression, Kind.METHOD_DECLARATION);
    if (method != null) {
      Iterator<ParameterTree> iterator = method.parameters().parameters().iterator();
      String name = name(expression);
      while (iterator.hasNext()) {
        ParameterTree next = iterator.next();
        String text = next.variableIdentifier().text();
        if (text.equals(name)) {
          return true;
        }
      }
    }
    return false;
  }

  @CheckForNull
  private static String name(Tree expression) {
    if (expression instanceof IdentifierTree) {
      return ((IdentifierTree) expression).text();
    }
    return null;
  }
}
