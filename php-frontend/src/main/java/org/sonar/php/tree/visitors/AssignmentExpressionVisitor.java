/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.tree.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public class AssignmentExpressionVisitor extends PHPVisitorCheck {

  private Map<Symbol, List<ExpressionTree>> assignedValuesBySymbol = new HashMap<>();
  private SymbolTable symbolTable;

  public AssignmentExpressionVisitor(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree assignment) {
    ExpressionTree variable = assignment.variable();
    Symbol symbol = symbolTable.getSymbol(variable);
    if (symbol != null) {
      if (!assignedValuesBySymbol.containsKey(symbol)) {
        assignedValuesBySymbol.put(symbol, new ArrayList<>());
      }
      assignedValuesBySymbol.get(symbol).add(assignment.value());
    }
    super.visitAssignmentExpression(assignment);
  }

  public Optional<ExpressionTree> getUniqueAssignedValue(Symbol symbol) {
    List<ExpressionTree> values = assignedValuesBySymbol.get(symbol);
    return values != null && values.size() == 1 ? Optional.of(values.get(0)) : Optional.empty();
  }

}
