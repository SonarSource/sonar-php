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
package org.sonar.php.checks.utils;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public abstract class FunctionUsageCheck extends PHPVisitorCheck {

  private Set<String> lowerCaseFunctionNames;

  protected abstract Set<String> lookedUpFunctionNames();

  @Override
  public void init() {
    super.init();
    lowerCaseFunctionNames = lookedUpFunctionNames().stream()
      .map(name -> name.toLowerCase(Locale.ROOT))
      .collect(Collectors.toSet());
  }

  protected abstract void checkFunctionCall(FunctionCallTree tree);

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (isExpectedFunction(tree.callee())) {
      checkFunctionCall(tree);
    }

    super.visitFunctionCall(tree);
  }

  private boolean isExpectedFunction(ExpressionTree callee) {
    return callee.is(Kind.NAMESPACE_NAME) &&
      lowerCaseFunctionNames.contains(((NamespaceNameTree) callee).qualifiedName().toLowerCase(Locale.ROOT));
  }

}
