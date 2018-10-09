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
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This CFG Test uses a meta-language to specify the expected structure of the CFG.
 *
 * Convention:
 *
 * 1. the metadata is specified as a 'block' function call with the form:
 * {@code block( [ id => 0, succ => [1,2], pred => [3] ] ); }
 * where the argument is a bracketed array with 3 elements:
 * - 'id' is the test id of the basic block
 * - 'succ' is a bracketed array of expected successor ids
 * - 'pred' [optional] is a bracketed array of expected predecessor ids
 *
 * 2. each basic block must contain the 'block' function call as the first statement
 *
 * Also check {@link ExpectedCfgStructure}
 */
public class ControlFlowGraphTest extends PHPTreeModelTest {

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
      "block( [ id => 0, succ => [1,2] ] );" +
      "if (a) {" +
      "  block( [ id => 1, succ => [2] ] );" +
      "}" +
      "block( [ id => 2, succ => [END] ] );");
  }

  /**
   * The predecessors are constructed based on the successors, so we should not test them all the time
   */
  @Test
  public void if_then_test_predecessors() {
    verifyBlockCfg("" +
      "block( [ id => 0, succ => [1,2], pred => [] ] );" +
      "foo();" +
      "if (a) {" +
      "  block( [ id => 1, succ => [2], pred => [0] ] );" +
      "}" +
      "block( [ id => 2, succ => [END], pred => [0,1] ] );");
  }

  @Test
  public void if_nested() {
    verifyBlockCfg("" +
      "block( [ id => 0, succ => [1,6] ] );" +
      "if (a?b:c) {" +
      "  block( [ id => 1, succ => [2,5] ] );" +
      "  if (b) {" +
      "    block( [ id => 2, succ => [3,4] ] );" +
      "    if (c) {" +
      "      block( [ id => 3, succ => [4] ] );" +
      "    }" +
      "    block( [ id => 4, succ => [5] ] );" +
      "  }" +
      "  block( [ id => 5, succ => [6] ] );" +
      "}" +
      "block( [ id => 6, succ => [END] ] );");
  }

  @Test
  public void if_multiple() {
    verifyBlockCfg("" +
      "block( [ id => 0, succ => [1,2] ] );" +
      "if (a) {" +
      "  block( [ id => 1, succ => [2] ] );" +
      "}" +
      "block( [ id => 2, succ => [3,4] ] );" +
      "if (b) {" +
      "  block( [ id => 3, succ => [4] ] );" +
      "}" +
      "block( [ id => 4, succ => [END] ] );");
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
