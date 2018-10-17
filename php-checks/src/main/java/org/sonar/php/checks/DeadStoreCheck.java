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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.cfg.CfgBlock;
import org.sonar.php.cfg.ControlFlowGraph;
import org.sonar.php.cfg.LiveVariablesAnalysis;
import org.sonar.php.cfg.LiveVariablesAnalysis.LiveVariables;
import org.sonar.php.cfg.LiveVariablesAnalysis.VariableUsage;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = "S1854")
public class DeadStoreCheck extends PHPSubscriptionCheck {

  private static final Set<String> BASIC_LITERAL_VALUES = ImmutableSet.of("true", "false", "1", "0", "0.0", "-1", "null", "''", "\"\"");
  private static final String MESSAGE_TEMPLATE = "Remove this useless assignment to local variable '%s'.";

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return ImmutableList.of(Kind.FUNCTION_DECLARATION,
      Kind.FUNCTION_EXPRESSION,
      Kind.METHOD_DECLARATION);
  }

  @Override
  public void visitNode(Tree tree) {
    ControlFlowGraph cfg = ControlFlowGraph.build(tree, context());
    if (cfg == null) {
      return;
    }
    LiveVariablesAnalysis lva = LiveVariablesAnalysis.analyze(cfg, context().symbolTable());
    Map<Symbol, Set<VariableUsage>> usagesInCfg = new HashMap<>();
    cfg.blocks().forEach(block -> addUsages(lva.getLiveVariables(block), usagesInCfg));
    Set<Symbol> readInCfg = getReadSymbols(usagesInCfg);
    cfg.blocks().forEach(block -> verifyBlock(block, lva.getLiveVariables(block), readInCfg));
  }

  /**
   * Bottom-up approach, keeping track of which variables will be read by successor elements.
   */
  private void verifyBlock(CfgBlock block, LiveVariables blockLiveVariables, Set<Symbol> readInCfg) {
    Set<Symbol> willBeRead = new HashSet<>(blockLiveVariables.getOut());
    for (Tree element : Lists.reverse(block.elements())) {
      Map<Symbol, VariableUsage> usagesInElement = blockLiveVariables.getVarUsages(element);
      reportDeadStores(element, usagesInElement, willBeRead, readInCfg);
    }
  }

  /**
   * We only report variables that are read at least once inside the CFG.
   * Local variables that are never read are reported by S1481 see {@link UnusedLocalVariableCheck}.
   */
  private void reportDeadStores(Tree element, Map<Symbol, VariableUsage> usagesInElement, Set<Symbol> willBeRead, Set<Symbol> readInCfg) {
    for (Map.Entry<Symbol, VariableUsage> symbolWithUsage : usagesInElement.entrySet()) {
      Symbol symbol = symbolWithUsage.getKey();
      VariableUsage usage = symbolWithUsage.getValue();
      if (usage == VariableUsage.WRITE) {
        if (isDeadStore(element, symbol, willBeRead) && readInCfg.contains(symbol)) {
          context().newIssue(this, element, String.format(MESSAGE_TEMPLATE, symbol.name()));
        }
        willBeRead.remove(symbol);
      } else if (usage.isAny(VariableUsage.READ, VariableUsage.READ_WRITE)) {
        willBeRead.add(symbol);
      }
    }
  }

  private static void addUsages(LiveVariables blockLiveVariables, Map<Symbol, Set<VariableUsage>> allUsagesInCfg) {
    Map<Symbol, Set<VariableUsage>> usages = blockLiveVariables.computeSymbolUsages();
    for (Map.Entry<Symbol, Set<VariableUsage>> symUsageInBlock : usages.entrySet()) {
      Set<VariableUsage> symUsagesInCfg = allUsagesInCfg.computeIfAbsent(symUsageInBlock.getKey(), x -> new HashSet<>());
      symUsagesInCfg.addAll(symUsageInBlock.getValue());
    }
  }

  private static Set<Symbol> getReadSymbols(Map<Symbol, Set<VariableUsage>> usagesInCfg) {
    Set<Symbol> readAtLeastOnce = new HashSet<>();
    for(Map.Entry<Symbol, Set<VariableUsage>> symUsages : usagesInCfg.entrySet()) {
      Set<VariableUsage> usages = symUsages.getValue();
      if (usages.contains(VariableUsage.READ) || usages.contains(VariableUsage.READ_WRITE)) {
        readAtLeastOnce.add(symUsages.getKey());
      }
    }
    return readAtLeastOnce;
  }


  private static boolean isDeadStore(Tree element, Symbol symbol, Set<Symbol> willBeRead) {
    return !willBeRead.contains(symbol) && !shouldSkip(element, symbol);
  }

  private static boolean shouldSkip(Tree element, Symbol symbol) {
    return symbol.hasModifier("global") || isInitializedToBasicValue(element);
  }

  private static boolean isInitializedToBasicValue(Tree element) {
    if (!element.is(Kind.EXPRESSION_STATEMENT)) {
      return false;
    }
    ExpressionTree inner = ((ExpressionStatementTree) element).expression();
    if (!inner.is(Kind.ASSIGNMENT)) {
      return false;
    }
    ExpressionTree rightmostValue = extractRightmostValue((AssignmentExpressionTree) inner);
    return isBasicValue(rightmostValue);
  }

  /**
   * For "$a = $b = foo();", it will return the tree for "foo()"
   */
  private static ExpressionTree extractRightmostValue(AssignmentExpressionTree assignment) {
    AssignmentExpressionTree rightMostAssignment = assignment;
    ExpressionTree rightValue = rightMostAssignment.value();
    while (rightValue.is(Kind.ASSIGNMENT)) {
      rightMostAssignment = (AssignmentExpressionTree) rightValue;
      rightValue = rightMostAssignment.value();
    }
    return rightValue;
  }

  private static boolean isBasicValue(ExpressionTree value) {
    if (value.is(Kind.NULL_LITERAL)) {
      return true;
    } else if (value.is(Kind.BOOLEAN_LITERAL, Kind.NUMERIC_LITERAL, Kind.REGULAR_STRING_LITERAL)) {
      String innerValue = ((LiteralTree) value).value().toLowerCase();
      return BASIC_LITERAL_VALUES.contains(innerValue) || innerValue.startsWith("0x0");
    } else if (value.is(Kind.UNARY_MINUS)) {
      return BASIC_LITERAL_VALUES.contains("-" + ((UnaryExpressionTree) value).expression().toString().toLowerCase());
    } else if (value.is(Kind.ARRAY_INITIALIZER_BRACKET, Kind.ARRAY_INITIALIZER_FUNCTION)) {
      return ((ArrayInitializerTree) value).arrayPairs().isEmpty();
    }
    return false;
  }

}
