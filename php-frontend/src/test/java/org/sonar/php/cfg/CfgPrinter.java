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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;

class CfgPrinter {

  private CfgPrinter() {
    // this is an utility class and should not be instantiated
  }

  static String toDot(ControlFlowGraph cfg) {
    StringBuilder sb = new StringBuilder();
    int blockId = 0;
    Map<CfgBlock, Integer> blockIds = new HashMap<>();
    for (CfgBlock block : cfg.blocks()) {
      blockIds.put(block, blockId);
      blockId++;
    }
    sb.append("cfg{");
    for (CfgBlock block : cfg.blocks()) {
      int id = blockIds.get(block);
      sb.append(id + "[label=\"" + blockLabel(cfg, block) + "\"];");
    }
    for (CfgBlock block : cfg.blocks()) {
      int id = blockIds.get(block);
      Set<CfgBlock> successors = block.successors();
      for (CfgBlock successor : successors) {
        String edgeLabel = "";
        if (block instanceof PhpCfgBranchingBlock) {
          PhpCfgBranchingBlock branching = (PhpCfgBranchingBlock) block;
          // branch value can be True or False
          boolean branchingValue = successor.equals(branching.trueSuccessor());
          edgeLabel = "[label=" + branchingValue + "]";
        }
        sb.append(id + "->" + blockIds.get(successor) + edgeLabel + ";");
      }
    }
    sb.append("}");

    return sb.toString();
  }

  private static String blockLabel(ControlFlowGraph cfg, CfgBlock block) {
    if (cfg.end().equals(block)) {
      return "<END>";
    }
    String extraInfo = "";

    if (!block.elements().isEmpty()) {
      Tree firstElement = block.elements().get(0);
      if (firstElement.is(Tree.Kind.EXPRESSION_STATEMENT)) {
        extraInfo += " { " + ((ExpressionStatementTree) firstElement).expression().toString() + " } ";
      }
    }
    if (block instanceof PhpCfgBranchingBlock) {
      IfStatementTree ifTree = (IfStatementTree) ((PhpCfgBranchingBlock) block).branchingTree();
      extraInfo += " (IF " + ifTree.condition().expression().toString() + " ) ";
    }

    return extraInfo;
  }

}
