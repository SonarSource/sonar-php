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
package org.sonar.php.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

public class CounterVisitor extends PHPSubscriptionCheck {

  private int functionCounter = 0;
  private int statementCounter = 0;
  private int classCounter = 0;

  private static final Kind[] STATEMENT_NODES = {
    Kind.USE_STATEMENT,
    Kind.NAMESPACE_STATEMENT,
    Kind.CONSTANT_DECLARATION,
    Kind.IF_STATEMENT,
    Kind.ALTERNATIVE_IF_STATEMENT,
    Kind.FOR_STATEMENT,
    Kind.FOREACH_STATEMENT,
    Kind.WHILE_STATEMENT,
    Kind.DO_WHILE_STATEMENT,
    Kind.SWITCH_STATEMENT,
    Kind.BREAK_STATEMENT,
    Kind.CONTINUE_STATEMENT,
    Kind.RETURN_STATEMENT,
    Kind.THROW_STATEMENT,
    Kind.TRY_STATEMENT,
    Kind.EMPTY_STATEMENT,
    Kind.EXPRESSION_STATEMENT,
    Kind.UNSET_VARIABLE_STATEMENT,
    Kind.LABEL,
    Kind.GOTO_STATEMENT,
    Kind.DECLARE_STATEMENT,
    Kind.STATIC_STATEMENT,
    Kind.GLOBAL_STATEMENT,
    Kind.CLASS_CONSTANT_PROPERTY_DECLARATION,
    Kind.CLASS_PROPERTY_DECLARATION,
    Kind.USE_TRAIT_DECLARATION
  };

  public CounterVisitor(Tree tree) {
    scanTree(tree);
  }

  @Override
  public List<Kind> nodesToVisit() {
    List<Kind> result = new ArrayList<>(Arrays.asList(MetricsVisitor.getFunctionNodes()));
    result.addAll(Arrays.asList(STATEMENT_NODES));
    result.addAll(Arrays.asList(MetricsVisitor.getClassNodes()));
    return result;
  }

  @Override
  public void visitNode(Tree tree) {
    if (tree.is(MetricsVisitor.getFunctionNodes())) {
      functionCounter++;

    } else if (tree.is(STATEMENT_NODES)) {
      statementCounter++;

    } else if (tree.is(MetricsVisitor.getClassNodes())) {
      classCounter++;
    }
  }

  public int getFunctionNumber() {
    return functionCounter;
  }

  public int getStatementNumber() {
    return statementCounter;
  }

  public int getClassNumber() {
    return classCounter;
  }

}
