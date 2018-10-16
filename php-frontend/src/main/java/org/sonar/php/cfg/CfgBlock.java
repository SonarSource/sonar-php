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

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.Tree;

/**
 * A node of a {@link ControlFlowGraph}.
 * Successors are the nodes which may be executed after this one.
 * Predecessors are the nodes which may be executed before this one.
 * Elements are instances of {@link Tree} which are evaluated one after the other.
 */
public interface CfgBlock {

  Set<CfgBlock> predecessors();

  Set<CfgBlock> successors();

  /**
   * @return block following this one if no jump is applied
   * Returns {@code null} if this block doesn't end with jump statement (break, continue, return, goto, throw)
   */
  @Nullable
  CfgBlock syntacticSuccessor();

  List<Tree> elements();

}
