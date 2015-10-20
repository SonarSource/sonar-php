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
package org.sonar.php.metrics;

import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    Kind.YIELD_STATEMENT,
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
    List<Kind> result = new ArrayList<>(Arrays.asList(MetricsVisitor.FUNCTION_NODES));
    result.addAll(Arrays.asList(STATEMENT_NODES));
    result.addAll(Arrays.asList(MetricsVisitor.CLASS_NODES));
    return result;
  }

  @Override
  public void visitNode(Tree tree) {
    if (tree.is(MetricsVisitor.FUNCTION_NODES)) {
      functionCounter++;

    } else if (tree.is(STATEMENT_NODES)) {
      statementCounter++;

    } else if (tree.is(MetricsVisitor.CLASS_NODES)) {
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
