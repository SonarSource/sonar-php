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
package org.sonar.php.utils;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpIssue;

/**

 * Dummy check for testing. Raises a precise issue on assignment expression and line issue on class declaration.
 * <p> Example:
 * <pre>
 *   {@literal<}?php
 *
 *     $a = 1;   // precise issue with 2 secondary locations (lhs and rhs)
 *     $b += 1;  // no issue
 *     class A{} // line issue
 * </pre>
 */
public class DummyCheck extends PHPVisitorCheck {

  public static final String MESSAGE = "message";

  private final Integer cost;

  public DummyCheck() {
    this(null);
  }

  public DummyCheck(Integer cost) {
    this.cost = cost;
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    if (tree.is(Tree.Kind.ASSIGNMENT)) {
      PhpIssue issue = context().newIssue(this, tree, MESSAGE)
        .secondary(tree.value(), null)
        .secondary(tree.variable(), null);
      if (cost != null) {
        issue.cost(cost);
      }
    }

    super.visitAssignmentExpression(tree);
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    context().newLineIssue(this, ((PHPTree) tree).getLine(), MESSAGE);
    super.visitClassDeclaration(tree);
  }
}
