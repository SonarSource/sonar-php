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
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.tree.statement.CaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ContinueStatementTree;
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.GotoStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.LabelTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;

/**
 * Builder of a {@link ControlFlowGraph} for a given {@link ScriptTree} or for the body of a function.
 * Implementation note: this class starts from the end and goes backward because it's easier to implement.
 */
class ControlFlowGraphBuilder {

  private final Set<PhpCfgBlock> blocks = new HashSet<>();
  private final PhpCfgEndBlock end = new PhpCfgEndBlock();

  private final LinkedList<Breakable> breakables = new LinkedList<>();
  private final Deque<PhpCfgBlock> throwTargets = new ArrayDeque<>();
  // key is label
  private final Map<String, PhpCfgBlock> labelledBlocks = new HashMap<>();
  // key is label, value is a list of blocks that jump to the label
  private final Map<String, List<PhpCfgBlock>> gotosWithoutTarget = new HashMap<>();
  private PhpCfgBlock start;

  ControlFlowGraphBuilder(List<? extends Tree> items) {
    throwTargets.push(end);
    start = build(items, createSimpleBlock(end));
    removeEmptyBlocks();
    blocks.add(end);
    computePredecessors();
  }

  ControlFlowGraph getGraph() {
    return new ControlFlowGraph(ImmutableSet.copyOf(blocks), start, end);
  }

  private void computePredecessors() {
    for (PhpCfgBlock block : blocks) {
      for (CfgBlock successor : block.successors()) {
        ((PhpCfgBlock) successor).addPredecessor(block);
      }
    }
  }

  private void removeEmptyBlocks() {
    Map<PhpCfgBlock, PhpCfgBlock> emptyBlockReplacements = new HashMap<>();
    for (PhpCfgBlock block : blocks) {
      if (block.elements().isEmpty() && block.successors().size() == 1) {
        PhpCfgBlock firstNonEmptySuccessor = block.skipEmptyBlocks();
        emptyBlockReplacements.put(block, firstNonEmptySuccessor);
      }
    }

    blocks.removeAll(emptyBlockReplacements.keySet());

    for (PhpCfgBlock block : blocks) {
      block.replaceSuccessors(emptyBlockReplacements);
    }

    start = emptyBlockReplacements.getOrDefault(start, start);
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
      case TRY_STATEMENT:
        return buildTryStatement((TryStatementTree) tree, currentBlock);
      case THROW_STATEMENT:
        return buildThrowStatement((ThrowStatementTree) tree, currentBlock);
      case RETURN_STATEMENT:
        return buildReturnStatement((ReturnStatementTree) tree, currentBlock);
      case BREAK_STATEMENT:
        return buildBreakStatement((BreakStatementTree) tree, currentBlock);
      case CONTINUE_STATEMENT:
        return buildContinueStatement((ContinueStatementTree) tree, currentBlock);
      case GOTO_STATEMENT:
        return buildGotoStatement((GotoStatementTree) tree, currentBlock);
      case DO_WHILE_STATEMENT:
        return buildDoWhileStatement((DoWhileStatementTree) tree, currentBlock);
      case WHILE_STATEMENT:
      case ALTERNATIVE_WHILE_STATEMENT:
        return buildWhileStatement((WhileStatementTree) tree, currentBlock);
      case IF_STATEMENT:
      case ALTERNATIVE_IF_STATEMENT:
        return buildIfStatement((IfStatementTree) tree, currentBlock);
      case FOR_STATEMENT:
      case ALTERNATIVE_FOR_STATEMENT:
        return buildForStatement((ForStatementTree) tree, currentBlock);
      case FOREACH_STATEMENT:
      case ALTERNATIVE_FOREACH_STATEMENT:
        return buildForEachStatement((ForEachStatementTree) tree, currentBlock);
      case BLOCK:
        return buildBlock((BlockTree) tree, currentBlock);
      case SWITCH_STATEMENT:
      case ALTERNATIVE_SWITCH_STATEMENT:
        return buildSwitchStatement((SwitchStatementTree) tree, currentBlock);
      case LABEL:
        return createLabelBlock((LabelTree) tree, currentBlock);
      case YIELD_STATEMENT:
      case GLOBAL_STATEMENT:
      case STATIC_STATEMENT:
      case DECLARE_STATEMENT:
      case UNSET_VARIABLE_STATEMENT:
      case EXPRESSION_LIST_STATEMENT:
      case FUNCTION_DECLARATION:
      case INTERFACE_DECLARATION:
      case TRAIT_DECLARATION:
      case USE_STATEMENT:
      case GROUP_USE_STATEMENT:
      case CONSTANT_DECLARATION:
      case NAMESPACE_STATEMENT:
      case EXPRESSION_STATEMENT:
        currentBlock.addElement(tree);
        return currentBlock;
      case INLINE_HTML:
      case EMPTY_STATEMENT:
      case CLASS_DECLARATION:
        return currentBlock;
      default:
        throw new UnsupportedOperationException("Not supported tree kind " + tree.getKind());
    }
  }

  private PhpCfgBlock buildSwitchStatement(SwitchStatementTree tree, PhpCfgBlock successor) {
    ForwardingBlock defaultBlock = createForwardingBlock();
    defaultBlock.setSuccessor(successor);
    PhpCfgBlock nextCase = defaultBlock;
    PhpCfgBlock caseBody = successor;
    addBreakable(successor, successor);
    for (SwitchCaseClauseTree caseTree : Lists.reverse(tree.cases())) {
      caseBody = buildSubFlow(caseTree.statements(), caseBody);
      if (caseTree.is(Tree.Kind.CASE_CLAUSE)) {
        PhpCfgBranchingBlock caseBranch = createBranchingBlock(caseTree, caseBody, nextCase);
        caseBranch.addElement(((CaseClauseTree) caseTree).expression());
        nextCase = caseBranch;
      } else {
        // default case
        defaultBlock.setSuccessor(caseBody);
      }
    }
    removeBreakable();
    PhpCfgBlock block = createSimpleBlock(nextCase);
    block.addElement(tree.expression());
    return block;
  }

  private PhpCfgBlock buildTryStatement(TryStatementTree tree, PhpCfgBlock successor) {
    PhpCfgBlock finallyOrSuccessor;
    if (tree.finallyBlock() != null) {
      finallyOrSuccessor = buildSubFlow(tree.finallyBlock().statements(), successor);
    } else {
      finallyOrSuccessor = successor;
    }

    List<PhpCfgBlock> catchBlocks = tree.catchBlocks().stream()
      .map(catchBlockTree -> buildSubFlow(catchBlockTree.block().statements(), finallyOrSuccessor))
      .collect(Collectors.toList());

    if (catchBlocks.isEmpty()) {
      throwTargets.push(finallyOrSuccessor);
    } else {
      throwTargets.push(catchBlocks.get(0));
    }
    Set<PhpCfgBlock> bodySuccessors = new HashSet<>(catchBlocks);
    bodySuccessors.add(finallyOrSuccessor);
    PhpCfgBlock tryBodyStartingBlock = build(tree.block().statements(), createMultiSuccessorBlock(bodySuccessors));
    throwTargets.pop();

    return tryBodyStartingBlock;
  }

  private PhpCfgBlock buildThrowStatement(ThrowStatementTree tree, PhpCfgBlock successor) {
    // taking "latest" throw target is an estimation
    // In real a matching `catch` clause should be found (by exception type)
    PhpCfgBlock simpleBlock = createBlockWithSyntacticSuccessor(throwTargets.peek(), successor);
    simpleBlock.addElement(tree);
    return simpleBlock;
  }

  private PhpCfgBlock buildReturnStatement(ReturnStatementTree tree, PhpCfgBlock successor) {
    PhpCfgBlock simpleBlock = createBlockWithSyntacticSuccessor(end, successor);
    simpleBlock.addElement(tree);
    return simpleBlock;
  }

  private PhpCfgBlock createLabelBlock(LabelTree tree, PhpCfgBlock currentBlock) {
    String label = tree.label().text();
    labelledBlocks.put(label, currentBlock);
    currentBlock.addElement(tree);
    List<PhpCfgBlock> gotoBlocks = gotosWithoutTarget.get(label);
    if (gotoBlocks != null) {
      gotoBlocks.forEach(gotoBlock -> gotoBlock.replaceSuccessor(end, currentBlock));
      gotosWithoutTarget.remove(label);
    }
    // create the block for the code above the label
    return createSimpleBlock(currentBlock);
  }

  private PhpCfgBlock buildGotoStatement(GotoStatementTree tree, PhpCfgBlock successor) {
    String label = tree.identifier().text();
    PhpCfgBlock gotoTarget = labelledBlocks.get(label);
    PhpCfgBlock newBlock;
    if (gotoTarget == null) {
      newBlock = createBlockWithSyntacticSuccessor(end, successor);
      List<PhpCfgBlock> gotosList = gotosWithoutTarget.computeIfAbsent(label, k -> new LinkedList<>());
      gotosList.add(newBlock);
    } else {
      newBlock = createBlockWithSyntacticSuccessor(gotoTarget, successor);
    }
    newBlock.addElement(tree);
    return newBlock;
  }

  private PhpCfgBlock buildBreakStatement(BreakStatementTree tree, PhpCfgBlock successor) {
    PhpCfgBlock newBlock = createBlockWithSyntacticSuccessor(getBreakable(tree.argument(), tree).breakTarget, successor);
    newBlock.addElement(tree);
    return newBlock;
  }

  private PhpCfgBlock buildContinueStatement(ContinueStatementTree tree, PhpCfgBlock successor) {
    PhpCfgBlock newBlock = createBlockWithSyntacticSuccessor(getBreakable(tree.argument(), tree).continueTarget, successor);
    newBlock.addElement(tree);
    return newBlock;
  }

  private Breakable getBreakable(@Nullable ExpressionTree argument, StatementTree jumpStmp) {
    try {
      int breakLevels = getBreakLevels(argument);
      return breakables.get(breakLevels - 1);
    } catch (IndexOutOfBoundsException e) {
      throw exception(jumpStmp, e);
    }
  }

  private static int getBreakLevels(@Nullable ExpressionTree argument) {
    if (argument == null) {
      return 1;
    }
    ExpressionTree levelsExpression = argument;
    if (levelsExpression.is(Kind.PARENTHESISED_EXPRESSION)) {
      levelsExpression = ((ParenthesisedExpressionTree) levelsExpression).expression();
    }
    if (!levelsExpression.is(Kind.NUMERIC_LITERAL)) {
      throw exception(argument);
    }
    try {
      int breakLevels = Integer.parseInt(((LiteralTree) levelsExpression).value());
      if (breakLevels == 0) {
        return 1;
      }
      return breakLevels;
    } catch (NumberFormatException e) {
      throw exception(argument, e);
    }
  }

  private static RecognitionException exception(Tree tree) {
    return new RecognitionException(((PHPTree) tree).getLine(), "Failed to build CFG");
  }

  private static RecognitionException exception(Tree tree, Throwable cause) {
    return new RecognitionException(((PHPTree) tree).getLine(), "Failed to build CFG", cause);
  }

  private PhpCfgBlock buildForEachStatement(ForEachStatementTree tree, PhpCfgBlock successor) {
    ForwardingBlock linkToCondition = createForwardingBlock();

    addBreakable(successor, linkToCondition);
    PhpCfgBlock loopBodyBlock = buildSubFlow(tree.statements(), linkToCondition);
    removeBreakable();

    PhpCfgBranchingBlock conditionBlock = createBranchingBlock(tree, loopBodyBlock, successor);
    conditionBlock.addElement(tree.expression());
    linkToCondition.setSuccessor(conditionBlock);
    return createSimpleBlock(conditionBlock);
  }

  private PhpCfgBlock buildForStatement(ForStatementTree tree, PhpCfgBlock successor) {
    // we need to reverse the 'update' and 'condition' and 'init' expressions
    // because they are sequential inside the ForStatementTree
    // (and not bottom-up like how we build the CFG)

    ForwardingBlock linkToCondition = createForwardingBlock();
    PhpCfgBlock updateBlock = createSimpleBlock(linkToCondition);
    Lists.reverse(tree.update()).forEach(updateBlock::addElement);

    addBreakable(successor, updateBlock);
    PhpCfgBlock loopBodyBlock = buildSubFlow(tree.statements(), updateBlock);
    removeBreakable();

    PhpCfgBranchingBlock conditionBlock = createBranchingBlock(tree, loopBodyBlock, successor);
    Lists.reverse(tree.condition()).forEach(conditionBlock::addElement);
    linkToCondition.setSuccessor(conditionBlock);

    PhpCfgBlock beforeFor = createSimpleBlock(conditionBlock);
    Lists.reverse(tree.init()).forEach(beforeFor::addElement);

    return beforeFor;
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
    PhpCfgBlock falseBlock = successor;
    if (tree.elseClause() != null) {
      falseBlock = buildSubFlow(tree.elseClause().statements(), successor);
    }
    if (!tree.elseifClauses().isEmpty()) {
      for (ElseifClauseTree elseifClause : Lists.reverse(tree.elseifClauses())) {
        falseBlock = buildElseIfStatement(elseifClause, successor, falseBlock);
      }
    }
    PhpCfgBlock trueBlock = buildSubFlow(tree.statements(), successor);
    PhpCfgBranchingBlock conditionBlock = createBranchingBlock(tree, trueBlock, falseBlock);
    conditionBlock.addElement(tree.condition().expression());
    return conditionBlock;
  }

  private PhpCfgBlock buildElseIfStatement(ElseifClauseTree tree, PhpCfgBlock ifSuccessor, PhpCfgBlock nextCondition) {
    PhpCfgBlock thenBlock = buildSubFlow(tree.statements(), ifSuccessor);
    PhpCfgBranchingBlock conditionBlock = createBranchingBlock(tree, thenBlock, nextCondition);
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

  private PhpCfgMultiSuccessorBlock createMultiSuccessorBlock(Set<PhpCfgBlock> successors) {
    PhpCfgMultiSuccessorBlock block = new PhpCfgMultiSuccessorBlock(successors);
    blocks.add(block);
    return block;
  }

  private PhpCfgBlock createSimpleBlock(PhpCfgBlock successor) {
    PhpCfgBlock block = new PhpCfgBlock(successor);
    blocks.add(block);
    return block;
  }

  private PhpCfgBlock createBlockWithSyntacticSuccessor(PhpCfgBlock successor, PhpCfgBlock syntacticSuccessor) {
    PhpCfgBlock block = new PhpCfgBlock(successor, syntacticSuccessor);
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
