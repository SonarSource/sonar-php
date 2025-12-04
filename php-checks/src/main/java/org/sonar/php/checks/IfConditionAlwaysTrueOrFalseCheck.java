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

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = IfConditionAlwaysTrueOrFalseCheck.KEY)
public class IfConditionAlwaysTrueOrFalseCheck extends PHPVisitorCheck {

  public static final String KEY = "S1145";
  private static final String MESSAGE = "Remove this \"if\" statement.";

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    ExpressionTree condition = tree.condition().expression();

    if (condition.is(Kind.BOOLEAN_LITERAL)) {
      context().newIssue(this, tree.ifToken(), tree.condition(), MESSAGE);
    }

    super.visitIfStatement(tree);
  }

}
