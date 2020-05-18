/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.plugins.php.api.visitors.PHPTreeSubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Rule(key = "S4143")
public class OverwrittenArrayElementCheck extends PHPSubscriptionCheck {

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return ImmutableList.of(Tree.Kind.BLOCK, Tree.Kind.SCRIPT);
  }

  private Map<String, Symbol> namesToSymbols = new HashMap<>();
  private Map<String, Map<String, Tree>> writtenAndUnread = new HashMap<>();

  @Override
  public void visitNode(Tree tree) {
    namesToSymbols.clear();
    writtenAndUnread.clear();

    List<StatementTree> statementTrees;
    if (tree.is(Tree.Kind.BLOCK)) {
      statementTrees = ((BlockTree) tree).statements();
    } else {
      statementTrees = ((ScriptTree) tree).statements();
    }

    for (StatementTree statementTree : statementTrees) {
      if (!isArrayKeyAssignmentStatement(statementTree)) {
        removeReadArrayKeys(statementTree);
        continue;
      }

      AssignmentExpressionTree assignmentExpressionTree = (AssignmentExpressionTree) ((ExpressionStatementTree) statementTree).expression();
      ArrayAccessTree arrayAccessTree = (ArrayAccessTree) assignmentExpressionTree.variable();
      String key = ((LiteralTree) arrayAccessTree.offset()).value();
      String variableName = ((VariableIdentifierTree) arrayAccessTree.object()).text();
      Symbol variableSymbol = context().symbolTable().getSymbol(arrayAccessTree.object());

      checkArrayKeyWrite(statementTree, assignmentExpressionTree, key, variableName, variableSymbol);

      updateWrittenAndUnread(statementTree, key, variableName, variableSymbol);
    }

    super.visitNode(tree);
  }

  private void checkArrayKeyWrite(StatementTree statementTree,
    AssignmentExpressionTree assignmentExpressionTree,
    String key,
    String variableName,
    Symbol variableSymbol) {
    if (writtenAndUnread.containsKey(variableName) &&
      writtenAndUnread.get(variableName).containsKey(key) &&
      !symbolWasUsedInTree(variableSymbol, assignmentExpressionTree.value())) {
      context().newIssue(this, statementTree, "Verify this is the key that was intended; it was already set before");
    }
  }

  private void updateWrittenAndUnread(StatementTree statementTree, String key, String variableName, Symbol variableSymbol) {
    Map<String, Tree> KeysToAssignmentMap;
    if (writtenAndUnread.containsKey(variableName)) {
      KeysToAssignmentMap = writtenAndUnread.get(variableName);
    } else {
      KeysToAssignmentMap = new HashMap<>();
    }
    KeysToAssignmentMap.put(key, statementTree);
    namesToSymbols.put(variableName, variableSymbol);
    writtenAndUnread.put(variableName, KeysToAssignmentMap);
  }

  private void removeReadArrayKeys(StatementTree statementTree) {
    Map<String, Map<String, Tree>> cleanedWrittenAndUnread = new HashMap<>();
    for (Map.Entry entry : writtenAndUnread.entrySet()) {
      if (!symbolWasUsedInTree(namesToSymbols.get(entry.getKey()), statementTree)) {
        cleanedWrittenAndUnread.put((String) entry.getKey(), (Map<String, Tree>) entry.getValue());
      }
    }
    writtenAndUnread = cleanedWrittenAndUnread;
  }

  private static boolean isArrayKeyAssignmentStatement(StatementTree tree) {
    if (!tree.is(Tree.Kind.EXPRESSION_STATEMENT)) {
      return false;
    }

    if (!((ExpressionStatementTree) tree).expression().is(Tree.Kind.ASSIGNMENT)) {
      return false;
    }

    AssignmentExpressionTree assignmentExpressionTree = (AssignmentExpressionTree) ((ExpressionStatementTree) tree).expression();

    return assignmentExpressionTree.variable().is(Tree.Kind.ARRAY_ACCESS) &&
      ((ArrayAccessTree) assignmentExpressionTree.variable()).object().is(Tree.Kind.VARIABLE_IDENTIFIER) &&
      ((ArrayAccessTree) assignmentExpressionTree.variable()).offset() != null &&
      ((ArrayAccessTree) assignmentExpressionTree.variable()).offset().is(Tree.Kind.NUMERIC_LITERAL, Tree.Kind.REGULAR_STRING_LITERAL);
  }

  private boolean symbolWasUsedInTree(Symbol symbol, Tree tree) {
    SymbolUsageVisitor checkVisitor = new SymbolUsageVisitor(symbol, context().symbolTable());
    checkVisitor.scanTree(tree);
    return checkVisitor.foundUsage;
  }

  class SymbolUsageVisitor extends PHPTreeSubscriber {
    private final Symbol symbol;
    private final SymbolTable symbolTable;
    private boolean foundUsage;

    public SymbolUsageVisitor(Symbol symbol, SymbolTable symbolTable) {
      this.symbol = symbol;
      this.symbolTable = symbolTable;
    }

    @Override
    public List<Tree.Kind> nodesToVisit() {
      return ImmutableList.of(Tree.Kind.VARIABLE_IDENTIFIER);
    }

    @Override
    public void visitNode(Tree tree) {
      Symbol currentSymbol = symbolTable.getSymbol(tree);

      if (!foundUsage) {
        foundUsage = currentSymbol == symbol;
      }
    }
  }
}
