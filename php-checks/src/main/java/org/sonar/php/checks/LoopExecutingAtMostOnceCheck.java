/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.cfg.CfgBlock;
import org.sonar.plugins.php.api.cfg.CfgBranchingBlock;
import org.sonar.plugins.php.api.cfg.ControlFlowGraph;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.tree.statement.GotoStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

import static org.sonar.php.tree.TreeUtils.findAncestorWithKind;
import static org.sonar.php.tree.TreeUtils.isDescendant;

@Rule(key = LoopExecutingAtMostOnceCheck.KEY)
public class LoopExecutingAtMostOnceCheck extends PHPVisitorCheck {

  public static final String KEY = "S1751";
  private static final String MESSAGE = "Refactor this loop to do more than one iteration.";

  // we don't include foreach loop because it is used to access first element of collection
  private static final Set<Tree.Kind> LOOPS = SetUtils.immutableSetOf(
    Tree.Kind.WHILE_STATEMENT,
    Tree.Kind.DO_WHILE_STATEMENT,
    Tree.Kind.FOR_STATEMENT,
    Tree.Kind.ALTERNATIVE_WHILE_STATEMENT,
    Tree.Kind.ALTERNATIVE_FOR_STATEMENT,
    // not loops but can contain break
    Tree.Kind.SWITCH_STATEMENT,
    Tree.Kind.ALTERNATIVE_SWITCH_STATEMENT
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
    if (loop == null || loop.is(Tree.Kind.SWITCH_STATEMENT, Tree.Kind.ALTERNATIVE_SWITCH_STATEMENT)) {
      return;
    }
    if (!isWhileTrue(loop) && !canExecuteMoreThanOnce(loop)) {
      jumpsByLoop.put(loop, tree);
    }
  }

  private static boolean isWhileTrue(Tree loop) {
    return loop.is(Tree.Kind.WHILE_STATEMENT, Tree.Kind.ALTERNATIVE_WHILE_STATEMENT)
      && CheckUtils.isTrueValue(((WhileStatementTree) loop).condition().expression());
  }

  private boolean canExecuteMoreThanOnce(Tree loop) {
    CfgBranchingBlock loopBlock = findLoopBlock(loop);
    if (loopBlock == null) {
      return true;
    }
    // try to find path in CFG from trueSuccessor of loopBlock to the loopBlock
    // if such path exists then loop can be executed multiple times
    Deque<CfgBlock> worklist = new ArrayDeque<>();
    worklist.add(loopBlock.trueSuccessor());
    Set<CfgBlock> seen = new HashSet<>();
    while (!worklist.isEmpty()) {
      CfgBlock b = worklist.pop();
      if (b.successors().contains(loopBlock)) {
        return true;
      }
      if (seen.add(b)) {
        b.successors().stream()
          // consider only paths within the loop body
          .filter(succ -> blockInsideLoop(succ, loop))
          .forEach(worklist::push);
      }
    }
    return false;
  }

  private static boolean blockInsideLoop(CfgBlock block, Tree loop) {
    return block.elements().isEmpty() || isDescendant(block.elements().get(0), loop);
  }

  @CheckForNull
  private CfgBranchingBlock findLoopBlock(Tree loop) {
    Tree treeWithFlow = findAncestorWithKind(loop, ControlFlowGraph.KINDS_WITH_CONTROL_FLOW);
    if (treeWithFlow == null) {
      // should never happen
      return null;
    }
    ControlFlowGraph cfg = ControlFlowGraph.build(treeWithFlow, context());
    if (cfg == null) {
      return null;
    }
    List<CfgBlock> loopBlocks = cfg.blocks().stream()
      .filter(CfgBranchingBlock.class::isInstance)
      .filter(b -> ((CfgBranchingBlock) b).branchingTree().equals(loop)).collect(Collectors.toList());

    return  ((CfgBranchingBlock) Iterables.getOnlyElement(loopBlocks));
  }
}
