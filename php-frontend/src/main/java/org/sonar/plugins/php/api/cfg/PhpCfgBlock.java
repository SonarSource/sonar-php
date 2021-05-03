/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.plugins.php.api.cfg;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.Tree;

class PhpCfgBlock implements CfgBlock {

  private Set<PhpCfgBlock> predecessors = new HashSet<>();
  private Set<CfgBlock> successors;
  private CfgBlock syntacticSuccessor;

  private LinkedList<Tree> elements = new LinkedList<>();

  private PhpCfgBlock(Set<PhpCfgBlock> successors, @Nullable PhpCfgBlock syntacticSuccessor) {
    this.successors = Collections.unmodifiableSet(successors);
    this.syntacticSuccessor = syntacticSuccessor;
  }

  PhpCfgBlock(PhpCfgBlock successor, PhpCfgBlock syntacticSuccessor) {
    this(SetUtils.immutableSetOf(successor), Preconditions.checkNotNull(syntacticSuccessor,
      "Syntactic successor cannot be null"));
  }

  PhpCfgBlock(Set<PhpCfgBlock> successors) {
    this(successors, null);
  }

  PhpCfgBlock(PhpCfgBlock successor) {
    this(SetUtils.immutableSetOf(successor));
  }

  PhpCfgBlock() {
    // needed by inheriting classes
  }

  @Override
  public Set<CfgBlock> predecessors() {
    return Collections.unmodifiableSet(predecessors);
  }

  @Override
  public Set<CfgBlock> successors() {
    return successors;
  }

  @Nullable
  @Override
  public CfgBlock syntacticSuccessor() {
    return syntacticSuccessor;
  }

  @Override
  public List<Tree> elements() {
    return Collections.unmodifiableList(elements);
  }

  public void addElement(Tree element) {
    Preconditions.checkArgument(element != null, "Cannot add a null element to a block");
    elements.addFirst(element);
  }

  /**
   * Replace successors based on a replacement map.
   * This method is used when we remove empty blocks:
   * we have to replace empty successors in the remaining blocks by non-empty successors.
   */
  void replaceSuccessors(Map<PhpCfgBlock, PhpCfgBlock> replacements) {
    successors = successors.stream()
      .map(successor -> replacement(successor, replacements))
      .collect(Collectors.toSet());
    if (syntacticSuccessor != null) {
      syntacticSuccessor = replacement(syntacticSuccessor, replacements);
    }
  }

  /**
   * Replace oldSucc with newSucc
   */
  void replaceSuccessor(PhpCfgBlock oldSucc, PhpCfgBlock newSucc) {
    Map<PhpCfgBlock, PhpCfgBlock> map = new HashMap<>();
    map.put(oldSucc, newSucc);
    replaceSuccessors(map);
  }

  static CfgBlock replacement(CfgBlock successor, Map<PhpCfgBlock, PhpCfgBlock> replacements) {
    PhpCfgBlock newSuccessor = replacements.get(successor);
    return newSuccessor == null ? successor : newSuccessor;
  }

  void addPredecessor(PhpCfgBlock predecessor) {
    predecessors.add(predecessor);
  }

  PhpCfgBlock skipEmptyBlocks() {
    Set<CfgBlock> skippedBlocks = new HashSet<>();
    PhpCfgBlock block = this;
    while (block.successors().size() == 1 && block.elements().isEmpty()) {
      PhpCfgBlock next = (PhpCfgBlock) block.successors().iterator().next();
      skippedBlocks.add(block);
      if (!skippedBlocks.contains(next)) {
        block = next;
      } else {
        return block;
      }
    }
    return block;
  }

  @Override
  public String toString() {
    if (elements.isEmpty()) {
      return "empty";
    }
    Tree firstElement = elements.get(0);
    if (firstElement.is(Tree.Kind.LABEL)) {
      firstElement = elements.get(1);
    }
    return firstElement.toString();
  }
}
