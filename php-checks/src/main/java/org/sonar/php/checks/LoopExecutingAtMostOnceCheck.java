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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.cfg.CfgBlock;
import org.sonar.php.cfg.CfgBranchingBlock;
import org.sonar.php.cfg.ControlFlowGraph;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.GotoStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

import static org.sonar.php.checks.utils.SyntacticEquivalence.areSyntacticallyEquivalent;
import static org.sonar.php.tree.TreeUtils.findAncestorWithKind;
import static org.sonar.php.tree.TreeUtils.isDescendant;

@Rule(key = LoopExecutingAtMostOnceCheck.KEY)
public class LoopExecutingAtMostOnceCheck extends PHPVisitorCheck {

  public static final String KEY = "S1751";
  private static final String MESSAGE = "Refactor this loop to do more than one iteration.";
  private static final Set<Tree.Kind> LOOPS = Sets.immutableEnumSet(
    Tree.Kind.WHILE_STATEMENT,
    Tree.Kind.DO_WHILE_STATEMENT,
    Tree.Kind.FOR_STATEMENT,
    Tree.Kind.FOREACH_STATEMENT,
    Tree.Kind.ALTERNATIVE_WHILE_STATEMENT,
    Tree.Kind.ALTERNATIVE_FOR_STATEMENT,
    Tree.Kind.ALTERNATIVE_FOREACH_STATEMENT
    );

  private ListMultimap<Tree, Tree> jumpsByLoop = ArrayListMultimap.create();

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    jumpsByLoop.clear();
    super.visitCompilationUnit(tree);
    reportIssues();
  }

  private void reportIssues() {
    jumpsByLoop.asMap().forEach((loop, jumps) -> {
      PreciseIssue preciseIssue = context().newIssue(this, ((PHPTree) loop).getFirstToken(), MESSAGE);
      jumps.forEach(jump -> preciseIssue.secondary(jump, "loop exit"));
    });
  }

  @Override
  public void visitBreakStatement(BreakStatementTree tree) {
    checkJump(tree);
  }

  @Override
  public void visitReturnStatement(ReturnStatementTree tree) {
    checkJump(tree);
  }

  @Override
  public void visitThrowStatement(ThrowStatementTree tree) {
    checkJump(tree);
  }

  @Override
  public void visitGotoStatement(GotoStatementTree tree) {
    checkJump(tree);
  }

  private void checkJump(Tree tree) {
    Tree loop = findAncestorWithKind(tree, LOOPS);
    if (loop != null && !canExecuteMoreThanOnce(loop) && !isForEachReturningFirstElement(loop, tree)) {
      jumpsByLoop.put(loop, tree);
    }
  }

  private static boolean isForEachReturningFirstElement(Tree loop, Tree jump) {
    if (!loop.is(Tree.Kind.FOREACH_STATEMENT, Tree.Kind.ALTERNATIVE_FOREACH_STATEMENT) || !jump.is(Tree.Kind.RETURN_STATEMENT)) {
      return false;
    }
    ReturnStatementTree returnTree = (ReturnStatementTree) jump;
    ForEachStatementTree forEachTree = (ForEachStatementTree) loop;
    if (hasMoreThanOneStatement(forEachTree)) {
      return false;
    }
    return areSyntacticallyEquivalent(returnTree.expression(), forEachTree.value())
      || areSyntacticallyEquivalent(returnTree.expression(), forEachTree.key());
  }

  private static boolean hasMoreThanOneStatement(ForEachStatementTree forEach) {
    if (forEach.statements().size() > 1) {
      return true;
    }
    if (forEach.statements().isEmpty()) {
      // just to be sure
      // will never happen in this rule, because we are hooked on jumps inside loops
      return false;
    }
    StatementTree body = forEach.statements().get(0);
    if (body.is(Tree.Kind.BLOCK)) {
      return ((BlockTree) body).statements().size() > 1;
    }
    return false;
  }

  private boolean canExecuteMoreThanOnce(Tree loop) {
    Tree treeWithFlow = findAncestorWithKind(loop, ControlFlowGraph.KINDS_WITH_CONTROL_FLOW);
    if (treeWithFlow == null) {
      // should never happen
      return true;
    }
    ControlFlowGraph cfg = ControlFlowGraph.build(treeWithFlow, context());
    if (cfg == null) {
      return true;
    }
    return cfg.blocks().stream()
      .filter(CfgBranchingBlock.class::isInstance)
      .filter(b -> ((CfgBranchingBlock) b).branchingTree().equals(loop))
      .anyMatch(b -> hasPredecessorInsideLoop(b, loop));
  }

  private static boolean hasPredecessorInsideLoop(CfgBlock block, Tree loop) {
    for (CfgBlock predecessor : block.predecessors()) {
      if (predecessor.elements().isEmpty()) {
        if (hasPredecessorInsideLoop(predecessor, loop)) {
          return true;
        } else {
          continue;
        }
      }
      Tree lastElement = Iterables.getLast(predecessor.elements());

      if (loop.is(Tree.Kind.FOR_STATEMENT, Tree.Kind.ALTERNATIVE_FOR_STATEMENT)) {
        ForStatementTree forTree = (ForStatementTree) loop;
        if (!forTree.update().isEmpty()
          && Iterables.getLast(forTree.update()).equals(lastElement)
          && !predecessor.predecessors().isEmpty()) {
          return true;
        }
      } else if (isDescendant(lastElement, loop)) {
        return true;
      }
    }
    return false;
  }

}
