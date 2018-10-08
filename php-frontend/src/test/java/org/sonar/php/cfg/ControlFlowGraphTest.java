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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This CFG Test uses a meta-language to specify the expected structure of the CFG.
 * See {@link BlockMetadata} for per-basic-block metadata
 *
 * Convention:
 *
 * 1. the metadata is specified as a method call with the form:
 * {@code block( [ id => 0, succ => [1,2], pred => [3] ] ); }
 * where:
 * - the name of the method is 'block' with one argument
 * - the argument is a bracketed array with 3 elements:
 * -- 'id' is the test id of the basic block
 * -- 'succ' is a bracketed array of expected successor ids
 * -- 'pred' is a bracketed array of expected predecessor ids
 *
 * 2. each basic block must contain the 'block' function call as the first statement
 *
 * 3. the metadata string is constructed with the {@link BlockString}
 */
public class ControlFlowGraphTest extends PHPTreeModelTest {

  /**
   * The predecessors are constructed based on the successors, so we should not test them all the time
   */
  @Test
  public void if_then_test_predecessors() {
    ControlFlowGraph cfg = build(functionTree(
      BlockString.withId(0).withSuccessorIds(1, 2).withPredecessorIds().build() +
        "$x = 1;" +
        "1 + 1;" +
        "if (a) {" +
        BlockString.withId(1).withSuccessorIds(2).withPredecessorIds(0).build() +
        "}" +
        BlockString.withId(2).withSuccessorIds("END").withPredecessorIds(0, 1).build()));

    assertThat(cfg.blocks()).hasSize(4);

    Map<String, BlockMetadata> data = MetadataParser.parse(cfg.blocks());

    CfgBlock startBlock = data.get("0").getBlock();
    assertThat(startBlock).isEqualTo(cfg.start());
    assertThat(startBlock).isExactlyInstanceOf(PhpCfgBranchingBlock.class);

    PhpCfgBranchingBlock ifBlock = (PhpCfgBranchingBlock) startBlock;
    assertThat(ifBlock.trueSuccessor()).isEqualTo(data.get("1").getBlock());
    assertThat(ifBlock.falseSuccessor()).isEqualTo(data.get("2").getBlock());

    assertThat(data.get("END").getBlock().predecessors()).hasSize(1);
    Validator.assertCfgStructure(data, CfgPrinter.toDot(cfg), true);
  }

  @Test
  public void if_nested() {
    ControlFlowGraph cfg = build(functionTree(
      BlockString.withId(0).withSuccessorIds(1, 6).build() +
        "if (a?b:c) {" +
        BlockString.withId(1).withSuccessorIds(2, 5).build() +
        "  if (b) {" +
        BlockString.withId(2).withSuccessorIds(3, 4).build() +
        "    if (c) {" +
        BlockString.withId(3).withSuccessorIds(4).build() +
        "    }" +
        BlockString.withId(4).withSuccessorIds(5).build() +
        "  }" +
        BlockString.withId(5).withSuccessorIds(6).build() +
        "}" +
        BlockString.withId(6).withSuccessorIds("END").build()));

    assertThat(cfg.blocks()).hasSize(8);

    Map<String, BlockMetadata> data = MetadataParser.parse(cfg.blocks());
    assertThat(data.get("0").getBlock()).isEqualTo(cfg.start());
    assertThat(data.get("END").getBlock().predecessors()).hasSize(1);
    Validator.assertCfgStructure(data, CfgPrinter.toDot(cfg), false);
  }

  @Test
  public void if_multiple() {
    ControlFlowGraph cfg = build(functionTree(
      BlockString.withId(0).withSuccessorIds(1, 2).build() +
        "if (a) {" +
        BlockString.withId(1).withSuccessorIds(2).build() +
        "};" +
        BlockString.withId(2).withSuccessorIds(3, 4).build() +
        " if (b) {" +
        BlockString.withId(3).withSuccessorIds(4).build() +
        " };" +
        BlockString.withId(4).withSuccessorIds("END").build()));

    assertThat(cfg.blocks()).hasSize(6);

    Map<String, BlockMetadata> data = MetadataParser.parse(cfg.blocks());
    assertThat(data.get("0").getBlock()).isEqualTo(cfg.start());
    assertThat(data.get("END").getBlock().predecessors()).hasSize(1);
    Validator.assertCfgStructure(data, CfgPrinter.toDot(cfg), false);
  }

  private ControlFlowGraph build(FunctionTree functionTree) {
    ControlFlowGraph cfg = ControlFlowGraph.build((BlockTree) functionTree.body());

    assertEndBlock(cfg);
    return cfg;
  }

  private void assertEndBlock(ControlFlowGraph cfg) {
    assertThat(cfg.end().successors()).isEmpty();
    assertThat(cfg.end().elements()).isEmpty();
  }

  private FunctionTree functionTree(String functionBody) {
    return parse("function f() { " + functionBody + " }", PHPLexicalGrammar.FUNCTION_DECLARATION);
  }

  private static class BlockString {
    int id;
    List<String> succ = new ArrayList<>();
    List<String> pred = new ArrayList<>();

    static BlockString withId(int id) {
      BlockString builder = new BlockString();
      builder.id = id;
      return builder;
    }

    BlockString withSuccessorIds(Object... successorIds) {
      for (Object successorId : successorIds) {
        this.succ.add(successorId.toString());
      }
      return this;
    }

    BlockString withPredecessorIds(Object... predecessorIds) {
      for (Object predecessorId : predecessorIds) {
        this.pred.add(predecessorId.toString());
      }
      return this;
    }

    String build() {
      return String.format("block([ id => %s, succ => [%s], pred => [%s] ]);",
        id,
        String.join(",", succ),
        String.join(",", pred));
    }
  }
}
