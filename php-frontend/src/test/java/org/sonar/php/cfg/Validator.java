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

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class Validator {

  private Validator() {
    // this is an utility class and should not be instantiated
  }

  static void assertCfgStructureWithPredecessors(ControlFlowGraph cfg, Map<String, ExpectedBlockStructure> expectedStructure, int numberOfBlocks) {
    assertCfgStructure(cfg, expectedStructure, numberOfBlocks, true);
  }

  static void assertCfgStructure(ControlFlowGraph cfg, Map<String, ExpectedBlockStructure> expectedStructure, int numberOfBlocks) {
    assertCfgStructure(cfg, expectedStructure, numberOfBlocks, false);
  }

  private static void assertCfgStructure(ControlFlowGraph cfg, Map<String, ExpectedBlockStructure> expectedStructure, int numberOfBlocks, boolean testPredecessors) {

    String debugDotNotation = CfgPrinter.toDot(cfg);

    assertThat(cfg.end().successors())
        .withFailMessage("END block should not have successors")
        .isEmpty();
    assertThat(cfg.end().elements())
        .withFailMessage("END block should not have elements")
        .isEmpty();
    assertThat(cfg.blocks())
        .withFailMessage(buildDebugMessage("size", debugDotNotation))
        .hasSize(numberOfBlocks);

    for (ExpectedBlockStructure expected : expectedStructure.values()) {
      if (expected.isEnd()) {
        continue;
      }

      String debugMessage = buildDebugMessage(expected.testId(), debugDotNotation);
      assertSuccessors(expected, expectedStructure, debugMessage);

      if (testPredecessors) {
        Set<CfgBlock> expectedPred = getCfgBlocksSet(expected.expectedPredIds(), expectedStructure);
        Set<CfgBlock> actualPred = expected.actualBlock().predecessors();
        assertThat(Sets.symmetricDifference(expectedPred, actualPred))
          .withFailMessage(debugMessage)
          .isEmpty();
      }
    }
  }

  private static void assertSuccessors(ExpectedBlockStructure expected,
    Map<String, ExpectedBlockStructure> expectedStructure,
    String debugMessage) {

    CfgBlock actualBlock = expected.actualBlock();

    if (actualBlock instanceof PhpCfgBranchingBlock) {

      PhpCfgBranchingBlock actualIfBlock = (PhpCfgBranchingBlock) actualBlock;
      List<CfgBlock> expectedSucc = getCfgBlocksList(expected.expectedSuccIds(), expectedStructure);
      assertThat(expectedSucc)
        .withFailMessage(debugMessage)
        .hasSize(2);
      assertThat(actualIfBlock.trueSuccessor())
        .withFailMessage(debugMessage)
        .isEqualTo(expectedSucc.get(0));
      assertThat(actualIfBlock.falseSuccessor())
        .withFailMessage(debugMessage)
        .isEqualTo(expectedSucc.get(1));

    } else {

      Set<CfgBlock> expectedSucc = getCfgBlocksSet(expected.expectedSuccIds(), expectedStructure);
      Set<CfgBlock> actualSucc = actualBlock.successors();
      assertThat(Sets.symmetricDifference(expectedSucc, actualSucc))
        .withFailMessage(debugMessage)
        .isEmpty();
    }
  }

  private static Set<CfgBlock> getCfgBlocksSet(Collection<String> ids, Map<String, ExpectedBlockStructure> data) {
    return getCfgBlocksStream(ids, data).collect(Collectors.toSet());
  }

  private static List<CfgBlock> getCfgBlocksList(Collection<String> ids, Map<String, ExpectedBlockStructure> data) {
    return getCfgBlocksStream(ids, data).collect(Collectors.toList());
  }

  private static Stream<CfgBlock> getCfgBlocksStream(Collection<String> ids, Map<String, ExpectedBlockStructure> data) {
    return ids.stream().map(id -> data.get(id).actualBlock());
  }

  private static String buildDebugMessage(String blockTestId, String cfgDotNotation) {
    StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
    stringJoiner.add("Not expected CFG structure. There is a problem with block " + blockTestId);
    stringJoiner.add("Use use a tool like http://www.webgraphviz.com/ to visualize the below graph in dot notation");
    stringJoiner.add("==========================================");
    stringJoiner.add(cfgDotNotation);
    stringJoiner.add("==========================================");
    return stringJoiner.toString();
  }
}
