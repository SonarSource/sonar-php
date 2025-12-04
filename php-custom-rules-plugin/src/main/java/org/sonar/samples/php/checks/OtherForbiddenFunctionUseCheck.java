/*
 * SonarQube PHP Plugin
 * Copyright (C) 2014-2025 SonarSource Sàrl
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
package org.sonar.samples.php.checks;

import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

/**
 * Example of implementation of a check by extending {@link PHPSubscriptionCheck}.
 * PHPSubscriptionCheck provides method {@link PHPSubscriptionCheck#visitNode} to visit nodes of the Abstract Syntax Tree
 * that represents the source code. Provide the list of nodes you want to visit through {@link PHPSubscriptionCheck#nodesToVisit}.
 * <p>
 * Those methods should be overridden to process information
 * related to node and issue can be created via the context that can be
 * accessed through {@link PHPVisitorCheck#context()}.
 */
@Rule(
  key = OtherForbiddenFunctionUseCheck.KEY,
  name = "Forbidden function should not be used.",
  tags = {"convention"},
  // Description can either be given in this annotation or through HTML name <ruleKey>.html located in package
  // src/resources/org/sonar/l10n/php/rules/<repositoryKey>
  description = "<p>The following functions should not be used:</p> <ul><li>fizz</li> <li>buzz</li></ul>")
public class OtherForbiddenFunctionUseCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S2";
  private static final Set<String> FORBIDDEN_FUNCTIONS = Set.of("fizz", "buzz");

  @Override
  public List<Kind> nodesToVisit() {
    return List.of(Kind.FUNCTION_CALL);
  }

  /**
   * Overriding method visiting the call expression to create an issue
   * each time a call to "fizz()" or "buzz()" is done.
   */
  @Override
  public void visitNode(Tree tree) {
    ExpressionTree callee = ((FunctionCallTree) tree).callee();

    if (callee.is(Kind.NAMESPACE_NAME) && FORBIDDEN_FUNCTIONS.contains(((NamespaceNameTree) callee).qualifiedName())) {
      context().newIssue(this, callee, "Remove the usage of this other forbidden function.");
    }
  }

}
