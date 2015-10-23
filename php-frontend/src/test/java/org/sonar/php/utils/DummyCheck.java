/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.utils;

import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.visitors.Issue;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

/**
 * Dummy check for testing. Raises an issue on assignment expression.
 * <p> Example:
 * <pre>
 *   {@literal<}?php
 *
 *     $a = 1;   // issue
 *     $b += 1;  // no issue
 * </pre>
 */
public class DummyCheck extends PHPVisitorCheck {

  public static final String KEY = "test";
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
      Issue issue = context().newIssue(KEY, MESSAGE).tree(tree);
      if (cost != null) {
        issue.cost(cost);
      }
    }

    super.visitAssignmentExpression(tree);
  }

}
