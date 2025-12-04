/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.checks.phpunit;

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
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;

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
      isDefinedFromParameter(expression);
  }

  private static boolean isStaticAccessWithName(ExpressionTree expression, String memberName) {
    if (expression instanceof MemberAccessTree tree) {
      return tree.isStatic() && memberName.equals(sourceVariableName(tree.member()));
    }
    return false;
  }

  private static boolean isDefinedFromParameter(ExpressionTree expression) {
    MethodDeclarationTree method = (MethodDeclarationTree) TreeUtils.findAncestorWithKind(expression, Kind.METHOD_DECLARATION);
    if (method != null) {
      String name = sourceVariableName(expression);
      for (ParameterTree parameter : method.parameters().parameters()) {
        String text = parameter.variableIdentifier().text();
        if (text.equals(name)) {
          return true;
        }
      }
    }
    return false;
  }

  @CheckForNull
  private static String sourceVariableName(Tree expression) {
    if (expression instanceof IdentifierTree identifier) {
      return identifier.text();
    } else if (expression instanceof ArrayAccessTree arrayAccessTree &&
      arrayAccessTree.object() instanceof VariableIdentifierTree variableIdentifierTree) {
        return variableIdentifierTree.token().text();
      }
    return null;
  }
}
