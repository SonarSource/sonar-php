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
import org.sonar.plugins.php.api.symbols.Symbol;

import static org.assertj.core.api.Assertions.assertThat;

class Validator {

  private static final String DEBUG_MESSAGE_TEMPLATE;

  static {
    StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
    stringJoiner.add("Not expected CFG structure. Hint: %s for '%s'.");
    stringJoiner.add("Use a tool like http://www.webgraphviz.com/ to visualize the below graph in dot notation");
    stringJoiner.add("==========================================");
    stringJoiner.add("digraph G { %s }");
    stringJoiner.add("==========================================");
    DEBUG_MESSAGE_TEMPLATE = stringJoiner.toString();
  }

  private Validator() {
    // this is an utility class and should not be instantiated
  }

  static void assertCfgStructure(ControlFlowGraph actualCfg) {
    ExpectedCfgStructure expectedCfg = ExpectedCfgStructure.parse(actualCfg.blocks());
    String debugDotNotation = CfgPrinter.toDot(actualCfg);

    assertThat(actualCfg.end().successors())
      .withFailMessage("END block should not have successors")
      .isEmpty();
    assertThat(actualCfg.end().elements())
      .withFailMessage("END block should not have elements")
      .isEmpty();
    assertThat(actualCfg.blocks())
      .withFailMessage(buildDebugMessage("size", "CFG", debugDotNotation))
      .hasSize(expectedCfg.size());

    for (CfgBlock actualBlock : actualCfg.blocks()) {
      if (actualBlock.equals(actualCfg.end())) {
        continue;
      }

      String blockTestId = expectedCfg.testId(actualBlock);
      assertSuccessors(actualBlock, expectedCfg, debugDotNotation);

      if (expectedCfg.hasNonEmptyPredecessors()) {
        Set<CfgBlock> expectedPred = getCfgBlocksSet(expectedCfg.expectedPred(actualBlock), expectedCfg);
        assertThat(actualBlock.predecessors())
          .withFailMessage(buildDebugMessage("predecessors", blockTestId, debugDotNotation))
          .containsOnlyElementsOf(expectedPred);
      }

      if (expectedCfg.hasNonEmptyElementNumbers()) {
        int actualElementNumber = actualBlock.elements().size();
        int expectedElementNumber = expectedCfg.expectedNumberOfElements(actualBlock);
        String message = String.format("Expecting %d elements instead of %d for '%s'",
          expectedElementNumber, actualElementNumber, blockTestId);
        assertThat(actualBlock.elements().size())
          .withFailMessage(message)
          .isEqualTo(expectedCfg.expectedNumberOfElements(actualBlock));
      }
    }
  }

  static void assertLiveVariables(ControlFlowGraph actualCfg, LiveVariablesAnalysis actualLva) {
    ExpectedCfgStructure expectedCfg = ExpectedCfgStructure.parse(actualCfg.blocks());
    String debugDotNotation = CfgPrinter.toDot(actualCfg);

    assertThat(actualCfg.blocks())
      .withFailMessage(buildDebugMessage("size", "CFG", debugDotNotation))
      .hasSize(expectedCfg.size());

    for (CfgBlock actualBlock : actualCfg.blocks()) {
      if (actualBlock.equals(actualCfg.end())) {
        continue;
      }

      String blockTestId = expectedCfg.testId(actualBlock);
      LiveVariablesAnalysis.LiveVariables actualLiveVariables = actualLva.getLiveVariables(actualBlock);
      assertVariablesAreEqual("Gen Variables", actualLiveVariables.getGen(), expectedCfg.expectedGenVariables(actualBlock), blockTestId, debugDotNotation);
      assertVariablesAreEqual("Killed Variables", actualLiveVariables.getKill(), expectedCfg.expectedKilledVariables(actualBlock), blockTestId, debugDotNotation);
      assertVariablesAreEqual("Live In Variables", actualLiveVariables.getIn(), expectedCfg.expectedLiveInVariables(actualBlock), blockTestId, debugDotNotation);
      assertVariablesAreEqual("Live Out Variables", actualLiveVariables.getOut(), expectedCfg.expectedLiveOutVariables(actualBlock), blockTestId, debugDotNotation);
    }
  }

  private static void assertVariablesAreEqual(String variableType, Set<Symbol> actualVariables, Set<String> expectedVariables, String blockTestId, String debugDotNotation) {
    int actualSize = actualVariables.size();
    int expectedSize = expectedVariables.size();
    assertThat(actualSize)
      .withFailMessage(buildDebugMessage(variableType + " size expected " + expectedSize + " and is " + actualSize, blockTestId, debugDotNotation))
      .isEqualTo(expectedSize);
    Set<String> actualVariableNames = actualVariables.stream().map(Symbol::name).collect(Collectors.toSet());
    assertThat(actualVariableNames)
      .withFailMessage(buildDebugMessage(variableType + " elements differ ", blockTestId, debugDotNotation))
      .containsOnlyElementsOf(expectedVariables);
  }

  private static void assertSuccessors(CfgBlock actualBlock, ExpectedCfgStructure expectedCfg, String debugDotNotation) {
    String blockTestId = expectedCfg.testId(actualBlock);

    if (actualBlock instanceof PhpCfgBranchingBlock) {

      PhpCfgBranchingBlock actualIfBlock = (PhpCfgBranchingBlock) actualBlock;
      List<CfgBlock> expectedSucc = getCfgBlocksList(expectedCfg.expectedSucc(actualBlock), expectedCfg);
      assertThat(expectedSucc)
        .withFailMessage(buildDebugMessage("branching block must have 2 elements", blockTestId, debugDotNotation))
        .hasSize(2);
      assertThat(actualIfBlock.trueSuccessor())
        .withFailMessage(buildDebugMessage("'true' branch successor", blockTestId, debugDotNotation))
        .isEqualTo(expectedSucc.get(0));
      assertThat(actualIfBlock.falseSuccessor())
        .withFailMessage(buildDebugMessage("'false' branch successor", blockTestId, debugDotNotation))
        .isEqualTo(expectedSucc.get(1));

    } else {

      Set<CfgBlock> expectedSucc = getCfgBlocksSet(expectedCfg.expectedSucc(actualBlock), expectedCfg);
      assertThat(actualBlock.successors())
        .withFailMessage(buildDebugMessage("successors", blockTestId, debugDotNotation))
        .containsOnlyElementsOf(expectedSucc);
    }

    String expectedSyntSucc = expectedCfg.expectedSyntSucc(actualBlock);

    if (expectedSyntSucc != null) {
      assertThat(actualBlock.syntacticSuccessor())
        .withFailMessage(buildDebugMessage("syntactic successor", blockTestId, debugDotNotation))
        .isEqualTo(expectedCfg.cfgBlock(expectedSyntSucc));

    } else {
      assertThat(actualBlock.syntacticSuccessor()).withFailMessage(buildDebugMessage("syntactic successor", blockTestId, debugDotNotation)).isNull();
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

  private static String buildDebugMessage(String hint, String blockId, String debugDotNotation) {
    return String.format(DEBUG_MESSAGE_TEMPLATE, hint, blockId, debugDotNotation);
  }

}
