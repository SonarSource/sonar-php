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

import java.util.Collections;
import java.util.Set;
import org.sonar.plugins.php.api.tree.statement.BlockTree;

/**
 * The <a href="https://en.wikipedia.org/wiki/Control_flow_graph">Control Flow Graph</a>
 * for a PHP script or for the body of a function.
 *
 * <p>Each node of the graph represents a list of elements which are executed sequentially.
 * Each node has:
 * <ul>
 * <li>one ore more successor blocks,</li>
 * <li>zero or more predecessor blocks.</li>
 * </ul>
 * </p>
 *
 * A Control Flow Graph has a single start node and a single end node.
 * The end node has no successor and no element.
 *
 */
public class ControlFlowGraph {

  private final CfgBlock start;
  private final PhpCfgEndBlock end;
  private final Set<PhpCfgBlock> blocks;

  ControlFlowGraph(Set<PhpCfgBlock> blocks, CfgBlock start, PhpCfgEndBlock end) {
    this.start = start;
    this.end = end;
    this.blocks = blocks;

    for (PhpCfgBlock block : blocks) {
      for (CfgBlock successor : block.successors()) {
        ((PhpCfgBlock) successor).addPredecessor(block);
      }
    }
  }

  public static ControlFlowGraph build(BlockTree body) {
    return new ControlFlowGraphBuilder().createGraph(body);
  }

  public CfgBlock start() {
    return start;
  }

  public CfgBlock end() {
    return end;
  }

  public Set<CfgBlock> blocks() {
    return Collections.unmodifiableSet(blocks);
  }
}
