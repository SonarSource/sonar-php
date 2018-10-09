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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class Validator {

  private Validator() {
    // this is an utility class and should not be instantiated
  }

  static void assertCfgStructure(ControlFlowGraph actualCfg) {
    ExpectedCfgStructure expectedCfg = ExpectedCfgStructure.parse(actualCfg.blocks());
    String debugDotNotation = CfgPrinter.toDot(actualCfg);

    assertThat(actualCfg.start()).isEqualTo(expectedCfg.cfgBlock("0"));
    assertThat(actualCfg.end().successors())
      .withFailMessage("END block should not have successors")
      .isEmpty();
    assertThat(actualCfg.end().elements())
      .withFailMessage("END block should not have elements")
      .isEmpty();
    assertThat(actualCfg.blocks())
      .withFailMessage(buildDebugMessage("size", debugDotNotation))
      .hasSize(expectedCfg.size());

    for (CfgBlock actualBlock : actualCfg.blocks()) {
      if (actualBlock.equals(actualCfg.end())) {
        continue;
      }

      String debugMessage = buildDebugMessage(expectedCfg.testId(actualBlock), debugDotNotation);
      assertSuccessors(actualBlock, expectedCfg, debugMessage);

      if (expectedCfg.hasNonEmptyPredecessors()) {
        Set<CfgBlock> expectedPred = getCfgBlocksSet(expectedCfg.expectedPred(actualBlock), expectedCfg);
        assertThat(actualBlock.predecessors())
          .withFailMessage(debugMessage)
          .containsExactlyElementsOf(expectedPred);
      }
    }
  }

  private static void assertSuccessors(CfgBlock actualBlock, ExpectedCfgStructure expectedCfg, String debugMessage) {

    if (actualBlock instanceof PhpCfgBranchingBlock) {

      PhpCfgBranchingBlock actualIfBlock = (PhpCfgBranchingBlock) actualBlock;
      List<CfgBlock> expectedSucc = getCfgBlocksList(expectedCfg.expectedSucc(actualBlock), expectedCfg);
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

      Set<CfgBlock> expectedSucc = getCfgBlocksSet(expectedCfg.expectedSucc(actualBlock), expectedCfg);
      assertThat(actualBlock.successors())
        .withFailMessage(debugMessage)
        .containsExactlyElementsOf(expectedSucc);
    }
  }

  private static Set<CfgBlock> getCfgBlocksSet(Collection<String> testIds, ExpectedCfgStructure expectedCfg) {
    return getCfgBlocksStream(testIds, expectedCfg).collect(Collectors.toSet());
  }

  private static List<CfgBlock> getCfgBlocksList(Collection<String> testIds, ExpectedCfgStructure expectedCfg) {
    return getCfgBlocksStream(testIds, expectedCfg).collect(Collectors.toList());
  }

  private static Stream<CfgBlock> getCfgBlocksStream(Collection<String> testIds, ExpectedCfgStructure expectedCfg) {
    return testIds.stream().map(expectedCfg::cfgBlock);
  }

  private static String buildDebugMessage(String blockTestId, String cfgDotNotation) {
    StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
    stringJoiner.add("Not expected CFG structure. There is a problem with block " + blockTestId);
    stringJoiner.add("Use a tool like http://www.webgraphviz.com/ to visualize the below graph in dot notation");
    stringJoiner.add("==========================================");
    stringJoiner.add("digraph G {");
    stringJoiner.add(cfgDotNotation);
    stringJoiner.add("}");
    stringJoiner.add("==========================================");
    return stringJoiner.toString();
  }
}
