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

class CfgPrinter {

  private CfgPrinter() {
    // this is an utility class and should not be instantiated
  }

  static String toDot(ControlFlowGraph cfg) {
    StringBuilder sb = new StringBuilder();
    int graphNodeId = 0;
    Map<CfgBlock, Integer> graphNodeIds = new HashMap<>();
    for (CfgBlock block : cfg.blocks()) {
      graphNodeIds.put(block, graphNodeId);
      graphNodeId++;
    }
    for (CfgBlock block : cfg.blocks()) {
      int id = graphNodeIds.get(block);
      sb.append(id + "[label=\"" + blockLabel(cfg, block) + "\"];");
    }
    for (CfgBlock block : cfg.blocks()) {
      int id = graphNodeIds.get(block);
      Set<CfgBlock> successors = block.successors();
      for (CfgBlock successor : successors) {
        String edgeLabel = "";
        if (block instanceof PhpCfgBranchingBlock) {
          PhpCfgBranchingBlock branching = (PhpCfgBranchingBlock) block;
          // branch value can be True or False
          boolean branchingValue = successor.equals(branching.trueSuccessor());
          edgeLabel = "[label=" + branchingValue + "]";
        }
        sb.append(id + "->" + graphNodeIds.get(successor) + edgeLabel + ";");
      }
      if (block.syntacticSuccessor() != null) {
        sb.append(id + "->" + graphNodeIds.get(block.syntacticSuccessor()) + "[style=dotted];");
      }
    }

    return sb.toString();
  }

  private static String blockLabel(ControlFlowGraph cfg, CfgBlock block) {
    if (cfg.end().equals(block)) {
      return "<END>";
    }

    String stringTree = "<not supported Tree; update CfgPrinter>";
    if (!block.elements().isEmpty()) {
      Tree firstElement = block.elements().get(0);
      if (firstElement.is(Tree.Kind.LABEL)) {
        firstElement = block.elements().get(1);
      }
      if (firstElement.is(Tree.Kind.EXPRESSION_STATEMENT)) {
        stringTree = ((ExpressionStatementTree) firstElement).expression().toString();
      } else if (firstElement.is(Tree.Kind.FUNCTION_CALL)) {
        stringTree = firstElement.toString();
      } else {
        System.out.println("Could not print " + firstElement.toString());
      }
    }

    return "Expected: " + stringTree;
  }

}
