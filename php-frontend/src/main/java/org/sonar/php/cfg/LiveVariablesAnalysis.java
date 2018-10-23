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
package org.sonar.php.cfg;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternElementTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

/**
 * This class provides information about symbols which are "live" (which value will be read) at some point of the program.
 * See https://en.wikipedia.org/wiki/Live_variable_analysis
 */
public class LiveVariablesAnalysis {

  private final Map<CfgBlock, LiveVariables> liveVariablesPerBlock = new HashMap<>();

  public static LiveVariablesAnalysis analyze(ControlFlowGraph cfg, SymbolTable symbols) {
    LiveVariablesAnalysis instance = new LiveVariablesAnalysis();
    instance.compute(cfg, symbols);
    return instance;
  }

  public LiveVariables getLiveVariables(CfgBlock block) {
    return liveVariablesPerBlock.get(block);
  }

  public Set<Symbol> getReadSymbols() {
    Set<Symbol> readAtLeastOnce = new HashSet<>();
    for (LiveVariables liveVariables : liveVariablesPerBlock.values()) {
      for (Map<Symbol, VariableUsage> symbolVariableUsageMap : liveVariables.variableUsagesPerElement.values()) {
        for (Map.Entry<Symbol, VariableUsage> symbolWithUsage : symbolVariableUsageMap.entrySet()) {
          if (symbolWithUsage.getValue().isRead) {
            readAtLeastOnce.add(symbolWithUsage.getKey());
          }
        }
      }
    }
    return readAtLeastOnce;
  }

  /**
   * See "worklist algorithm" in http://www.cs.cornell.edu/courses/cs4120/2013fa/lectures/lec26-fa13.pdf
   * An alternative terminology for "kill/gen" is "def/use"
   */
  private void compute(ControlFlowGraph cfg, SymbolTable symbols) {
    cfg.blocks().forEach(block -> liveVariablesPerBlock.put(block, LiveVariables.build(block, symbols)));
    Deque<CfgBlock> workList = new ArrayDeque<>(cfg.blocks());
    while (!workList.isEmpty()) {
      CfgBlock currentBlock = workList.pop();
      LiveVariables liveVariables = liveVariablesPerBlock.get(currentBlock);
      boolean liveInHasChanged = liveVariables.propagate(liveVariablesPerBlock);
      if (liveInHasChanged) {
        currentBlock.predecessors().forEach(workList::push);
      }
    }
  }

  public static final class VariableUsage {
    private boolean isRead = false;
    private boolean isWrite = false;

    public boolean isWrite() {
      return isWrite;
    }

    public boolean isRead() {
      return isRead;
    }
  }

  /**
   * Holds the information about the liveness of variables for one basic block.
   * - The 'gen' and 'kill' sets are built once, in the 'init' method.
   * - The 'in' and 'out' sets are modified by the 'propagate' method.
   */
  public static class LiveVariables {

    private final CfgBlock block;
    private final Map<Tree, Map<Symbol, VariableUsage>> variableUsagesPerElement;

    /**
     * variables that are being read in the block
     */
    private final Set<Symbol> gen = new HashSet<>();

    /**
     * variables that are being written in the block
     */
    private final Set<Symbol> kill = new HashSet<>();

    /**
     * The Live-In variables are variables which has values that:
     * - are needed by this block
     * OR
     * - are needed by a successor block and are not killed in this block.
     */
    private Set<Symbol> in = new HashSet<>();

    /**
     * The Live-Out variables are variables which are needed by successors.
     */
    private Set<Symbol> out = new HashSet<>();

    private LiveVariables(CfgBlock block) {
      this.block = block;
      this.variableUsagesPerElement = new HashMap<>();
    }

    public Set<Symbol> getIn() {
      return ImmutableSet.copyOf(in);
    }

    public Set<Symbol> getOut() {
      return ImmutableSet.copyOf(out);
    }

    public Map<Symbol, VariableUsage> getVariableUsages(Tree tree) {
      return ImmutableMap.copyOf(variableUsagesPerElement.get(tree));
    }

    Set<Symbol> getGen() {
      return ImmutableSet.copyOf(gen);
    }

    Set<Symbol> getKill() {
      return ImmutableSet.copyOf(kill);
    }

    /**
     * Builds a new LiveVariables instance for the given block and initializes the 'kill' and 'gen' symbol sets.
     */
    static LiveVariables build(CfgBlock block, SymbolTable symbols) {
      LiveVariables instance = new LiveVariables(block);
      instance.init(block, symbols);
      return instance;
    }

    private void init(CfgBlock block, SymbolTable symbols) {
      // 'writtenOnly' has variables that are WRITE-ONLY inside at least one element
      // (as opposed to 'kill' which can have a variable that inside an element is both READ and WRITTEN)
      Set<Symbol> writtenOnly = new HashSet<>();
      for (Tree element : block.elements()) {
        Map<Symbol, VariableUsage> variableUsages = ReadWriteVisitor.getVariableUsages(element, symbols);
        variableUsagesPerElement.put(element, variableUsages);
        computeGenAndKill(writtenOnly, variableUsages);
      }
    }

    /**
     * This has side effects on 'writtenOnly'
     */
    private void computeGenAndKill(Set<Symbol> writtenOnly, Map<Symbol, VariableUsage> variableUsages) {
      for (Map.Entry<Symbol, VariableUsage> variableUsage : variableUsages.entrySet()) {
        Symbol variable = variableUsage.getKey();
        VariableUsage usage = variableUsage.getValue();
        if (usage.isRead && !writtenOnly.contains(variable)) {
          gen.add(variable);
        }
        if (usage.isWrite) {
          kill.add(variable);
          // We do not know the order of execution inside a block element (if 'write' is done before 'read'),
          // so we only add if we know for sure there was only 'write' done for the element
          if (!usage.isRead) {
            writtenOnly.add(variable);
          }
        }
      }
    }

    /**
     * Propagates backwards: first computes the 'out' set, then the 'in' set.
     */
    boolean propagate(Map<CfgBlock, LiveVariables> liveVariablesPerBlock) {
      out.clear();
      block.successors().stream()
        .map(liveVariablesPerBlock::get)
        .map(LiveVariables::getIn)
        .forEach(out::addAll);
      // in = gen + (out - kill)
      Set<Symbol> newIn = new HashSet<>(gen);
      newIn.addAll(Sets.difference(out, kill));
      boolean inHasChanged = !newIn.equals(in);
      in = newIn;
      return inHasChanged;
    }

  }

  private static class ReadWriteVisitor extends PHPVisitorCheck {
    private final SymbolTable symbols;
    private final Map<Symbol, VariableUsage> variables = new HashMap<>();

    ReadWriteVisitor(SymbolTable symbols) {
      this.symbols = symbols;
    }

    static Map<Symbol, VariableUsage> getVariableUsages(Tree tree, SymbolTable symbols) {
      ReadWriteVisitor visitor = new ReadWriteVisitor(symbols);
      tree.accept(visitor);
      return visitor.variables;
    }

    @Override
    public void visitAssignmentExpression(AssignmentExpressionTree tree) {
      if (tree.getKind() != Tree.Kind.ASSIGNMENT) {
        visitReadVariable(tree.variable());
      }
      if (!visitAssignedVariable(tree.variable())) {
        tree.variable().accept(this);
      }
      tree.value().accept(this);
    }

    @Override
    public void visitArrayAssignmentPatternElement(ArrayAssignmentPatternElementTree tree) {
      if (visitAssignedVariable(tree.variable())) {
        Tree key = tree.key();
        if (key != null) {
          visitAssignedVariable(key);
        }
      } else {
        super.visitArrayAssignmentPatternElement(tree);
      }
    }

    @Override
    public void visitVariableIdentifier(VariableIdentifierTree tree) {
      visitReadVariable(tree);
      super.visitVariableIdentifier(tree);
    }

    @Override
    public void visitPrefixExpression(UnaryExpressionTree tree) {
      if (tree.is(Tree.Kind.PREFIX_INCREMENT) || tree.is(Tree.Kind.PREFIX_DECREMENT)) {
        visitUnaryExpression(tree);
      } else {
        super.visitPrefixExpression(tree);
      }
    }

    @Override
    public void visitPostfixExpression(UnaryExpressionTree tree) {
      if (tree.is(Tree.Kind.POSTFIX_INCREMENT) || tree.is(Tree.Kind.POSTFIX_DECREMENT)) {
        visitUnaryExpression(tree);
      } else {
        super.visitPostfixExpression(tree);
      }
    }

    private void visitUnaryExpression(UnaryExpressionTree tree) {
      visitReadVariable(tree.expression());
      visitAssignedVariable(tree.expression());
    }

    private boolean visitAssignedVariable(Tree tree) {
      if (!tree.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        return false;
      }
      Symbol varSym = symbols.getSymbol(tree);
      if (isLocalVariable(varSym)) {
        VariableUsage usage = variables.computeIfAbsent(varSym, s -> new VariableUsage());
        usage.isWrite = true;
        return true;
      }
      return false;
    }

    private void visitReadVariable(Tree tree) {
      if (!tree.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        return;
      }
      Symbol varSym = symbols.getSymbol(tree);
      if (isLocalVariable(varSym)) {
        VariableUsage usage = variables.computeIfAbsent(varSym, s -> new VariableUsage());
        usage.isRead = true;
      }
    }

    private static boolean isLocalVariable(@Nullable Symbol symbol) {
      return symbol != null && symbol.kind() == Symbol.Kind.VARIABLE;
    }
  }
}
