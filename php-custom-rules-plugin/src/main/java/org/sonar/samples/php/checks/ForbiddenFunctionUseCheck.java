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
package org.sonar.samples.php.checks;

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

/**
 * Example of implementation of a check by extending {@link PHPVisitorCheck}.
 * PHPVisitorCheck provides methods to visit nodes of the Abstract Syntax Tree
 * that represents the source code.
 * <p>
 * Those methods can be overridden to process information
 * related to node and issue can be created via the context that can be
 * accessed through {@link PHPVisitorCheck#context()}.
 */
@Rule(
  key = ForbiddenFunctionUseCheck.KEY,
  name = "Forbidden function should not be used.",
  tags = {"convention"}
// Description can either be given in this annotation or through HTML name <ruleKey>.html located in package
// src/resources/org/sonar/l10n/php/rules/<repositoryKey>
// description = "<p>The following functions should not be used:</p> <ul><li>foo</li> <li>bar</li></ul>"
)
public class ForbiddenFunctionUseCheck extends PHPVisitorCheck {

  public static final String KEY = "S1";
  private static final Set<String> FORBIDDEN_FUNCTIONS = Set.of("foo", "bar");

  /**
   * Overriding method visiting the call expression to create an issue
   * each time a call to "foo()" or "bar()" is done.
   */
  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();

    if (callee.is(Kind.NAMESPACE_NAME) && FORBIDDEN_FUNCTIONS.contains(((NamespaceNameTree) callee).qualifiedName())) {
      context().newIssue(this, callee, "Remove the usage of this forbidden function.");
    }

    // super method must be called in order to visit function call node's children
    super.visitFunctionCall(tree);
  }

}
