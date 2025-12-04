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

import java.util.Collections;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = SilencedErrorsCheck.KEY)
public class SilencedErrorsCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S2002";
  private static final String MESSAGE = "Remove the '@' symbol from this function call to un-silence errors.";

  @Override
  public List<Kind> nodesToVisit() {
    return Collections.singletonList(Kind.ERROR_CONTROL);
  }

  @Override
  public void visitNode(Tree tree) {
    context().newIssue(this, ((UnaryExpressionTree) tree).operator(), MESSAGE);
  }

}
