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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;

/**
 * Builder of a {@link ControlFlowGraph} for a given {@link ScriptTree} or for the body of a function.
 * Implementation note: this class starts from the end and goes backward because it's easier to implement.
 */
class ControlFlowGraphBuilder {

  private final Set<PhpCfgBlock> blocks = new HashSet<>();
  private final PhpCfgEndBlock end = new PhpCfgEndBlock();

  ControlFlowGraph createGraph(BlockTree body) {
    return createGraph(body.statements());
  }

  ControlFlowGraph createGraph(ScriptTree scriptTree) {
    return createGraph(scriptTree.statements());
  }

  private ControlFlowGraph createGraph(List<? extends Tree> items) {
    // TODO add end to throw targets
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

  private PhpCfgBlock buildDoWhileStatement(DoWhileStatementTree tree, PhpCfgBlock successor) {
    ForwardingBlock linkToCondition = createForwardingBlock();
    PhpCfgBlock loopBodyBlock = buildSubFlow(ImmutableList.of(tree.statement()), linkToCondition);
    PhpCfgBranchingBlock conditionBlock = createBranchingBlock(tree, loopBodyBlock, successor);
    conditionBlock.addElement(tree.condition().expression());
    linkToCondition.setSuccessor(conditionBlock);
    return createSimpleBlock(loopBodyBlock);
  }

  private PhpCfgBlock buildWhileStatement(WhileStatementTree tree, PhpCfgBlock successor) {
    ForwardingBlock linkToCondition = createForwardingBlock();
    PhpCfgBlock loopBodyBlock = buildSubFlow(tree.statements(), linkToCondition);
    PhpCfgBranchingBlock conditionBlock = createBranchingBlock(tree, loopBodyBlock, successor);
    conditionBlock.addElement(tree.condition().expression());
    linkToCondition.setSuccessor(conditionBlock);
    return createSimpleBlock(conditionBlock);
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

}
