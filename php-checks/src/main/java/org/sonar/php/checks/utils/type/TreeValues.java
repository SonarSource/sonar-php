/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.php.checks.utils.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;

public class TreeValues {

  public final List<ExpressionTree> values;
  private final SymbolTable symbolTable;

  private TreeValues(List<ExpressionTree> values, SymbolTable symbolTable) {
    this.values = values;
    this.symbolTable = symbolTable;
  }

  public static TreeValues of(ExpressionTree node, SymbolTable symbolTable) {
    return new TreeValues(Collections.singletonList(node), symbolTable);
  }

  public TreeValues lookupPossibleValues(ExpressionTree tree) {
    List<ExpressionTree> result = new ArrayList<>();
    ExpressionTree node = CheckUtils.skipParenthesis(tree);
    result.add(node);
    if (node.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      Symbol symbol = symbolTable.getSymbol(node);
      if (symbol != null) {
        Stream.concat(Stream.of(symbol.declaration()), symbol.usages().stream().map(SyntaxToken::getParent))
          .map(TreeValues::usageValue)
          .filter(Objects::nonNull)
          .forEach(result::add);
      }
    }
    return new TreeValues(result, symbolTable);
  }

  @Nullable
  private static ExpressionTree usageValue(Tree tree) {
    Tree parent = tree.getParent();
    if (parent.is(Tree.Kind.ASSIGNMENT, Tree.Kind.ASSIGNMENT_BY_REFERENCE)) {
      AssignmentExpressionTree assignment = (AssignmentExpressionTree) parent;
      if (assignment.variable() == tree) {
        return CheckUtils.skipParenthesis(assignment.value());
      }
    } else if (parent.is(Tree.Kind.FOREACH_STATEMENT)) {
      ForEachStatementTree forEachStatement = (ForEachStatementTree) parent;
      if (forEachStatement.value() == tree) {
        return forEachStatement.value();
      }
    }
    return null;
  }

}
