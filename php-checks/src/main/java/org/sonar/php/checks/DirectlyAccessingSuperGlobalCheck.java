/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = DirectlyAccessingSuperGlobalCheck.KEY)
public class DirectlyAccessingSuperGlobalCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Do not access \"%s\" directly.";
  public static final String KEY = "S2043";

  private static final Set<String> SUPER_GLOBAL_REQUIRING_SANITIZATION = Set.of(
    "$_COOKIE", "$_ENV", "$_FILES", "$_GET", "$_POST", "$_REQUEST", "$_SERVER");

  @Override
  public void visitVariableIdentifier(VariableIdentifierTree tree) {
    checkVariable(tree.variableExpression().token());
    super.visitVariableIdentifier(tree);
  }

  private void checkVariable(SyntaxToken variable) {
    String name = variable.text();
    if (SUPER_GLOBAL_REQUIRING_SANITIZATION.contains(name)) {
      context().newIssue(this, variable, String.format(MESSAGE, name));
    }
  }

}
