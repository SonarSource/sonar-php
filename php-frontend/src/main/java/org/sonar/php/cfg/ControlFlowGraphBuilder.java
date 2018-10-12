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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.RecognitionException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.tree.statement.ContinueStatementTree;
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;

/**
 * Builder of a {@link ControlFlowGraph} for a given {@link ScriptTree} or for the body of a function.
 * Implementation note: this class starts from the end and goes backward because it's easier to implement.
 */
class ControlFlowGraphBuilder {

  private final Set<PhpCfgBlock> blocks = new HashSet<>();
  private final PhpCfgEndBlock end = new PhpCfgEndBlock();

  private ArrayDeque<Breakable> breakables = new ArrayDeque<>();

  ControlFlowGraph createGraph(BlockTree body) {
    return createGraph(body.statements());
  }

  ControlFlowGraph createGraph(ScriptTree scriptTree) {
    return createGraph(scriptTree.statements());
  }

  private ControlFlowGraph createGraph(List<? extends Tree> items) {
    // TODO add end to throw targets
    breakables.clear();
    PhpCfgBlock start = build(items, createSimpleBlock(end));
    removeEmptyBlocks();
    blocks.add(end);
    return new ControlFlowGraph(blocks, start, end);
  }

  private void removeEmptyBlocks() {
    Map<PhpCfgBlock, PhpCfgBlock> emptyBlockReplacements = new HashMap<>();
    for (PhpCfgBlock block : blocks) {
      if (block.elements().isEmpty()) {
        PhpCfgBlock firstNonEmptySuccessor = block.skipEmptyBlocks();
        emptyBlockReplacements.put(block, firstNonEmptySuccessor);
      }
    }

    blocks.removeAll(emptyBlockReplacements.keySet());

    for (PhpCfgBlock block : blocks) {
      block.replaceSuccessors(emptyBlockReplacements);
    }
  }

  private PhpCfgBlock build(List<? extends Tree> trees, PhpCfgBlock successor) {
    PhpCfgBlock currentBlock = successor;
    for (Tree tree : Lists.reverse(trees)) {
      currentBlock = build(tree, currentBlock);
    }

    return currentBlock;
  }

  private PhpCfgBlock build(Tree tree, PhpCfgBlock currentBlock) {
    switch (tree.getKind()) {
      case THROW_STATEMENT:
        return buildThrowStatement((ThrowStatementTree) tree);
      case RETURN_STATEMENT:
        return buildReturnStatement((ReturnStatementTree) tree);
      case BREAK_STATEMENT:
        return buildBreakStatement((BreakStatementTree) tree);
      case CONTINUE_STATEMENT:
        return buildContinueStatement((ContinueStatementTree) tree);
      case DO_WHILE_STATEMENT:
        return buildDoWhileStatement((DoWhileStatementTree) tree, currentBlock);
      case WHILE_STATEMENT:
      case ALTERNATIVE_WHILE_STATEMENT:
        return buildWhileStatement((WhileStatementTree) tree, currentBlock);
      case IF_STATEMENT:
        return buildIfStatement((IfStatementTree) tree, currentBlock);
      case BLOCK:
        return buildBlock((BlockTree) tree, currentBlock);
      case EXPRESSION_STATEMENT:
        currentBlock.addElement(tree);
        return currentBlock;
      default:
        throw new UnsupportedOperationException("Not supported tree kind " + tree.getKind());
    }
  }

  private PhpCfgBlock buildThrowStatement(ThrowStatementTree tree) {
    PhpCfgBlock simpleBlock = createSimpleBlock(end);
    simpleBlock.addElement(tree);
    return simpleBlock;
  }

  private PhpCfgBlock buildReturnStatement(ReturnStatementTree tree) {
    PhpCfgBlock simpleBlock = createSimpleBlock(end);
    simpleBlock.addElement(tree);
    return simpleBlock;
  }

  private PhpCfgBlock buildBreakStatement(BreakStatementTree tree) {
    PhpCfgBlock newBlock = createSimpleBlock(getBreakable(tree.argument(), tree).breakTarget);
    newBlock.addElement(tree);
    return newBlock;
  }

  private PhpCfgBlock buildContinueStatement(ContinueStatementTree tree) {
    PhpCfgBlock newBlock = createSimpleBlock(getBreakable(tree.argument(), tree).continueTarget);
    newBlock.addElement(tree);
    return newBlock;
  }

  private Breakable getBreakable(@Nullable ExpressionTree argument, StatementTree jumpStmp) {
    try {
      if (argument != null) {
        if (!argument.is(Kind.NUMERIC_LITERAL)) {
          throw exception(jumpStmp);
        }
        int breakLevels = getBreakLevels((LiteralTree) argument);
        Iterator<Breakable> breakableIterator = breakables.iterator();
        Breakable breakable = breakableIterator.next();
        while (breakLevels > 1) {
          breakable = breakableIterator.next();
          breakLevels--;
        }
        return breakable;

      } else {
        return breakables.element();
      }

    } catch (NumberFormatException | NoSuchElementException e) {
      throw exception(jumpStmp, e);
    }
  }

  private static int getBreakLevels(LiteralTree argument) {
    int breakLevels = Integer.parseInt(argument.value());
    if (breakLevels == 0) {
      breakLevels = 1;
    }
    return breakLevels;
  }

  private static RecognitionException exception(Tree tree) {
    return new RecognitionException(((PHPTree) tree).getLine(), "Failed to build CFG");
  }

  private static RecognitionException exception(Tree tree, Throwable cause) {
    return new RecognitionException(((PHPTree) tree).getLine(), "Failed to build CFG", cause);
  }

  private PhpCfgBlock buildDoWhileStatement(DoWhileStatementTree tree, PhpCfgBlock successor) {
    ForwardingBlock linkToBody = createForwardingBlock();
    PhpCfgBranchingBlock conditionBlock = createBranchingBlock(tree, linkToBody, successor);
    conditionBlock.addElement(tree.condition().expression());

    addBreakable(successor, conditionBlock);
    PhpCfgBlock loopBodyBlock = buildSubFlow(ImmutableList.of(tree.statement()), conditionBlock);
    removeBreakable();

    linkToBody.setSuccessor(loopBodyBlock);
    return createSimpleBlock(loopBodyBlock);
  }

  private PhpCfgBlock buildWhileStatement(WhileStatementTree tree, PhpCfgBlock successor) {
    ForwardingBlock linkToCondition = createForwardingBlock();
    addBreakable(successor, linkToCondition);
    PhpCfgBlock loopBodyBlock = buildSubFlow(tree.statements(), linkToCondition);
    removeBreakable();
    PhpCfgBranchingBlock conditionBlock = createBranchingBlock(tree, loopBodyBlock, successor);
    conditionBlock.addElement(tree.condition().expression());
    linkToCondition.setSuccessor(conditionBlock);
    return createSimpleBlock(conditionBlock);
  }

  private void removeBreakable() {
    breakables.pop();
  }

  private void addBreakable(PhpCfgBlock breakTarget, PhpCfgBlock continueTarget) {
    breakables.push(new Breakable(breakTarget, continueTarget));
  }

  private ForwardingBlock createForwardingBlock() {
    ForwardingBlock block = new ForwardingBlock();
    blocks.add(block);
    return block;
  }

  private PhpCfgBlock buildBlock(BlockTree block, PhpCfgBlock successor) {
    return build(block.statements(), successor);
  }

  private PhpCfgBlock buildIfStatement(IfStatementTree tree, PhpCfgBlock successor) {
    PhpCfgBlock thenBlock = buildSubFlow(tree.statements(), successor);
    PhpCfgBranchingBlock conditionBlock = createBranchingBlock(tree, thenBlock, successor);
    conditionBlock.addElement(tree.condition().expression());
    return conditionBlock;
  }

  private PhpCfgBlock buildSubFlow(List<StatementTree> subFlowTree, PhpCfgBlock successor) {
    return build(subFlowTree, createSimpleBlock(successor));
  }

  private PhpCfgBranchingBlock createBranchingBlock(Tree branchingTree, PhpCfgBlock trueSuccessor, PhpCfgBlock falseSuccessor) {
    PhpCfgBranchingBlock block = new PhpCfgBranchingBlock(branchingTree, trueSuccessor, falseSuccessor);
    blocks.add(block);
    return block;
  }

  private PhpCfgBlock createSimpleBlock(PhpCfgBlock successor) {
    PhpCfgBlock block = new PhpCfgBlock(successor);
    blocks.add(block);
    return block;
  }

  private static class ForwardingBlock extends PhpCfgBlock {

    private PhpCfgBlock successor;

    @Override
    public ImmutableSet<CfgBlock> successors() {
      Preconditions.checkState(successor != null, "No successor was set on %s", this);
      return ImmutableSet.of(successor);
    }

    @Override
    public void addElement(Tree element) {
      throw new UnsupportedOperationException("Cannot add an element to a forwarding block");
    }

    void setSuccessor(PhpCfgBlock successor) {
      this.successor = successor;
    }

    @Override
    public void replaceSuccessors(Map<PhpCfgBlock, PhpCfgBlock> replacements) {
      throw new UnsupportedOperationException("Cannot replace successors for a forwarding block");
    }
  }

  private static class Breakable {
    PhpCfgBlock breakTarget;
    PhpCfgBlock continueTarget;

    Breakable(PhpCfgBlock breakTarget, PhpCfgBlock continueTarget) {
      this.breakTarget = breakTarget;
      this.continueTarget = continueTarget;
    }
  }
}
