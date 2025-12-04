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
package org.sonar.php.checks;

import java.util.Locale;
import java.util.Optional;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

public class CakePhpDebugModeCheckPart extends PHPVisitorCheck implements CheckBundlePart {

  private static final String MESSAGE = "Make sure this debug feature is deactivated before delivering the code in production.";

  private static final String CONFIG_FUNCTION = "Configure::config".toLowerCase(Locale.ROOT);
  private static final String WRITE_FUNCTION = "Configure::write".toLowerCase(Locale.ROOT);
  private CheckBundle bundle;

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = CheckUtils.getLowerCaseFunctionName(tree);

    if (CONFIG_FUNCTION.equals(functionName)) {
      checkArgs(tree, "name", "engine");
    } else if (WRITE_FUNCTION.equals(functionName)) {
      checkArgs(tree, "config", "value");
    }
    super.visitFunctionCall(tree);
  }

  private void checkArgs(FunctionCallTree tree, String arg1Name, String arg2Name) {
    CallArgumentTree firstArgument = CheckUtils.argument(tree, arg1Name, 0).orElse(null);
    CallArgumentTree secondArgument = CheckUtils.argument(tree, arg2Name, 1).orElse(null);
    if (firstArgument != null && secondArgument != null
      && firstArgument.value().is(Tree.Kind.REGULAR_STRING_LITERAL)
      && trimQuotes((LiteralTree) firstArgument.value()).equals("debug")
      && isTrue(secondArgument.value())) {
      context().newIssue(getBundle(), tree, MESSAGE);
    }
  }

  private static boolean isTrue(ExpressionTree tree) {
    if (tree.is(Tree.Kind.BOOLEAN_LITERAL, Tree.Kind.NUMERIC_LITERAL, Tree.Kind.REGULAR_STRING_LITERAL) &&
      CheckUtils.isTrueValue(tree)) {
      return true;
    }
    if (tree.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      Optional<ExpressionTree> uniqueAssignedValue = CheckUtils.uniqueAssignedValue((VariableIdentifierTree) tree);
      if (uniqueAssignedValue.isPresent()) {
        ExpressionTree expressionTree = uniqueAssignedValue.get();
        return CheckUtils.isTrueValue(expressionTree);
      }
    }
    return false;
  }

  @Override
  public void setBundle(CheckBundle bundle) {
    this.bundle = bundle;
  }

  @Override
  public CheckBundle getBundle() {
    return bundle;
  }
}
