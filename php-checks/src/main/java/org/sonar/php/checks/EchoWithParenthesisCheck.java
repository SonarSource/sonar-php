/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.checks;

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = EchoWithParenthesisCheck.KEY)
public class EchoWithParenthesisCheck extends FunctionUsageCheck {

  public static final String KEY = "S2041";
  private static final String MESSAGE = "Remove the parentheses from this \"echo\" call.";

  @Override
  protected Set<String> lookedUpFunctionNames() {
    return Set.of("echo");
  }

  @Override
  protected void checkFunctionCall(FunctionCallTree tree) {
    if (isParenthesized(tree)) {
      newIssue(tree.callee(), MESSAGE);
    }
  }

  private static boolean isParenthesized(FunctionCallTree tree) {
    return tree.callArguments().size() == 1 && tree.callArguments().get(0).value().is(Tree.Kind.PARENTHESISED_EXPRESSION);
  }

}
