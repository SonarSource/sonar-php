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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.sonar.plugins.php.api.tree.Tree;

class PhpCfgBlock implements CfgBlock {

  private Set<PhpCfgBlock> predecessors = new HashSet<>();
  private PhpCfgBlock successor;

  private LinkedList<Tree> elements = new LinkedList<>();

  PhpCfgBlock(PhpCfgBlock successor) {
    Preconditions.checkArgument(successor != null, "Successor cannot be null");
    this.successor = successor;
  }

  PhpCfgBlock() {
  }

  @Override
  public Set<CfgBlock> predecessors() {
    return Collections.<CfgBlock>unmodifiableSet(predecessors);
  }

  @Override
  public Set<CfgBlock> successors() {
    return ImmutableSet.<CfgBlock>of(successor);
  }

  @Override
  public List<Tree> elements() {
    return Collections.unmodifiableList(elements);
  }

  public void addElement(Tree element) {
    Preconditions.checkArgument(element != null, "Cannot add a null element to a block");
    elements.addFirst(element);
  }

  void addPredecessor(PhpCfgBlock predecessor) {
    this.predecessors.add(predecessor);
  }

}
