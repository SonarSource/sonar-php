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
package org.sonar.php.tree.visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.php.tree.impl.VariableIdentifierTreeImpl;
import org.sonar.php.tree.symbols.SymbolImpl;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternElementTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ListExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public class AssignmentExpressionVisitor extends PHPVisitorCheck {

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree assignment) {
    ExpressionTree lhs = assignment.variable();
    ExpressionTree rhs = assignment.value();

    if (lhs.is(Tree.Kind.LIST_EXPRESSION)) {
      handleListAssignment((ListExpressionTree) lhs, rhs);
    } else {
      assign(lhs, rhs);
    }

    super.visitAssignmentExpression(assignment);
  }

  private void handleListAssignment(ListExpressionTree lhs, ExpressionTree rhs) {
    List<ExpressionTree> values = new ArrayList<>();
    if (rhs.is(Tree.Kind.ARRAY_INITIALIZER_BRACKET, Tree.Kind.ARRAY_INITIALIZER_FUNCTION)) {
      List<ArrayPairTree> valueArrayParis = ((ArrayInitializerTree) rhs).arrayPairs();
      if (valueArrayParis.stream().anyMatch(p -> p.key() != null)) {
        values = Collections.emptyList();
      } else {
        values = valueArrayParis.stream().map(ArrayPairTree::value).toList();
      }
    }

    int index = 0;
    final int numValues = values.size();
    for (Optional<ArrayAssignmentPatternElementTree> element : lhs.elements()) {
      if (element.isPresent()) {
        if (index >= numValues || element.get().key() != null) {
          assignToUnknown(element.get().variable());
          continue;
        }

        assign(element.get().variable(), values.get(index));
      }
      index++;
    }
  }

  private void assign(Tree lhs, ExpressionTree rhs) {
    SymbolImpl symbol = getSymbol(lhs);
    if (symbol != null) {
      symbol.assignValue(rhs);
    }
  }

  private void assignToUnknown(Tree lhs) {
    SymbolImpl symbol = getSymbol(lhs);
    if (symbol != null) {
      symbol.assignUnknown();
    }
  }

  @CheckForNull
  private static SymbolImpl getSymbol(Tree tree) {
    if (tree.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      return ((VariableIdentifierTreeImpl) tree).symbol();
    }
    return null;
  }
}
