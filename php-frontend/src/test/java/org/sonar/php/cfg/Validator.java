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
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class Validator {

  private Validator() {
    // this is an utility class and should not be instantiated
  }

  static void assertCfgStructure(Map<String, BlockMetadata> data, String dotNotation, boolean testPredecessors) {

    for (BlockMetadata metadata : data.values()) {
      if (metadata.isEnd()) {
        continue;
      }

      if (testPredecessors) {
        Set<CfgBlock> expectedPred = getCfgBlocks(metadata.getExpectedPredecessorIds(), data);
        Set<CfgBlock> actualPred = metadata.getBlock().predecessors();
        assertAreTheSame(expectedPred, actualPred,
            buildErrorMessage(metadata.getId(), dotNotation, "predecessors"));
      }

      Set<CfgBlock> expectedSucc = getCfgBlocks(metadata.getExpectedSuccessorIds(), data);
      Set<CfgBlock> actualSucc = metadata.getBlock().successors();
      assertAreTheSame(expectedSucc, actualSucc,
        buildErrorMessage(metadata.getId(), dotNotation, "successors"));
    }
  }

  private static String buildErrorMessage(String id, String dotNotation, String hint) {
    StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
    stringJoiner.add("CFG structure is not the expected one for block " + id + " " + hint);
    stringJoiner.add("Use use a tool like http://www.webgraphviz.com/ to visualize the below graph in dot notation");
    stringJoiner.add("==========================================");
    stringJoiner.add(dotNotation);
    stringJoiner.add("==========================================");
    return stringJoiner.toString();
  }

  private static void assertAreTheSame(Set<CfgBlock> left, Set<CfgBlock> right, String errorMessage) {
    assertThat(Sets.symmetricDifference(left, right)).withFailMessage(errorMessage).isEmpty();
  }

  private static Set<CfgBlock> getCfgBlocks(Set<String> ids, Map<String, BlockMetadata> data) {
    return ids.stream().map(id -> data.get(id).getBlock()).collect(Collectors.toSet());
  }

}
