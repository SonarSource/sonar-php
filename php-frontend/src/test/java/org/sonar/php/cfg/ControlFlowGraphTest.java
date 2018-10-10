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

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This CFG Test uses a meta-language to specify the expected structure of the CFG.
 *
 * Convention:
 *
 * 1. the metadata is specified as a function call with the form:
 *
 * {@code blockId( succ = [1,2], pred = [3], elem = 1 ); }
 * where the argument is a bracketed array with 3 elements:
 * - 'succ' is a bracketed array of expected successor ids. For branching blocks, the true successor must be first.
 * - 'pred' [optional] is a bracketed array of expected predecessor ids
 * - 'elem' [optional] is the number of expected elements in the block
 *
 * 2. each basic block must contain a function call with this structure as the first statement
 *
 * 3. the name of the function is the identifier of the basic block
 *
 * Also check {@link ExpectedCfgStructure}
 */
public class ControlFlowGraphTest extends PHPTreeModelTest {

  @Test
  public void simple_while() {
    verifyBlockCfg("" +
      "before( succ = [cond] );" +
      "while (cond( succ = [body, after] )) {" +
      "  body( succ = [cond] );" +
      "}" +
      "after( succ = [END] );");

    verifyBlockCfg("" +
      "before( succ = [cond] );" +
      "while (cond( succ = [body, after] )) :" +
      "  body( succ = [cond] );" +
      "endwhile;" +
      "after( succ = [END] );");
  }

  @Test
  public void while_with_nested_if() {
    verifyBlockCfg("" +
      "before( succ = [whileCond] );" +
      "while (whileCond( succ = [ifCond, after] )) {" +
      "  if (ifCond( succ = [ifBody, whileCond] )) {" +
      "    ifBody( succ = [whileCond] );" +
      "  }" +
      "}" +
      "after( succ = [END] );");
  }

  @Test
  public void if_with_nested_while() {
    verifyBlockCfg("" +
      "before( succ = [ifBody, after], elem = 2 );" +
      "if (condition) {" +
      "  ifBody( succ = [whileCond], elem = 1 );" +
      "  while (whileCond( succ = [whileBody, ifBodyTail], elem = 1 )) {" +
      "    whileBody( succ = [whileCond], elem = 1 );" +
      "  }" +
      "  ifBodyTail( succ = [after], elem = 1 );" +
      "}" +
      "after( succ = [END], elem = 1 );");
  }

  @Test
  public void test_start_is_first_block() {
    ControlFlowGraph cfg = cfgForBlock("" +
      "foo();" +
      "if (a) {" +
      "  $x = 1;" +
      "}");
    CfgBlock startBlock = cfg.start();
    assertThat(startBlock.elements()).isNotEmpty();
    Tree firstElement = startBlock.elements().get(0);
    assertThat(firstElement.getKind()).isEqualTo(Tree.Kind.EXPRESSION_STATEMENT);
    ExpressionStatementTree statement = (ExpressionStatementTree) firstElement;
    assertThat(statement.expression().getKind()).isEqualTo(Tree.Kind.FUNCTION_CALL);
  }

  @Test
  public void test_branching_tree() {
    ControlFlowGraph cfg = cfgForBlock("" +
      "if (a) {" +
      "  qix();" +
      "}");
    CfgBlock block = cfg.start();
    assertThat(block instanceof PhpCfgBranchingBlock).isTrue();
    PhpCfgBranchingBlock ifBlock = (PhpCfgBranchingBlock) block;
    assertThat(ifBlock.branchingTree().getKind()).isEqualTo(Tree.Kind.IF_STATEMENT);
  }

  @Test
  public void test_empty_block_removal() {
    ControlFlowGraph cfg = cfgForBlock("" +
      "if (a) {" +
      "  bar();" +
      "  if (b) {" +
      "    qix();" +
      "  }" +
      "}");
    assertThat(cfg.end().predecessors()).hasSize(3);
  }

  @Test
  public void test_with_script_tree() {
    verifyScriptTreeCfg("" +
      "b0( succ = [b1, b2], elem = 3 );" +
      "foo();" +
      "if (a) {" +
      "  b1( succ = [b2], elem = 1 );" +
      "}" +
      "b2( succ = [END], elem = 1 );");
  }

  /**
   * The predecessors are constructed based on the successors, so we should not test them all the time
   */
  @Test
  public void if_then_test_predecessors() {
    verifyBlockCfg("" +
      "b0( succ = [b1, b2], pred = [] );" +
      "foo();" +
      "if (a) {" +
      "  b1( succ = [b2], pred = [b0] );" +
      "}" +
      "b2( succ = [END], pred = [b0, b1] );");
  }

  @Test
  public void if_nested() {
    verifyBlockCfg("" +
      "b0( succ = [b1, b6] );" +
      "if (a?b:c) {" +
      "  b1( succ = [b2, b5] );" +
      "  if (b) {" +
      "    b2( succ = [b3, b4] );" +
      "    if (c) {" +
      "      b3( succ = [b4] );" +
      "    }" +
      "    b4( succ = [b5] );" +
      "  }" +
      "  b5( succ = [b6] );" +
      "}" +
      "b6( succ = [END] );");
  }

  @Test
  public void if_multiple() {
    verifyBlockCfg("" +
      "b0( succ = [b1, b2] );" +
      "if (a) {" +
      "  b1( succ = [b2] );" +
      "}" +
      "b2( succ = [b3, b4] );" +
      "if (b) {" +
      "  b3( succ = [b4] );" +
      "}" +
      "b4( succ = [END] );");
  }

  private void verifyBlockCfg(String functionBody) {
    Validator.assertCfgStructure(cfgForBlock(functionBody));
  }

  private void verifyScriptTreeCfg(String body) {
    ScriptTree tree = parse("<?php " + body, PHPLexicalGrammar.SCRIPT);
    Validator.assertCfgStructure(ControlFlowGraph.build(tree));
  }

  private ControlFlowGraph cfgForBlock(String functionBody) {
    FunctionTree functionTree = parse("function f() { " + functionBody + " }", PHPLexicalGrammar.FUNCTION_DECLARATION);
    return ControlFlowGraph.build((BlockTree) functionTree.body());
  }
}
