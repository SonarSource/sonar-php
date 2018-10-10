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

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.sonar.plugins.php.api.tree.Tree;

class PhpCfgBranchingBlock extends PhpCfgBlock implements CfgBranchingBlock {

  private PhpCfgBlock trueSuccessor;
  private PhpCfgBlock falseSuccessor;

  private Tree branchingTree;

  public PhpCfgBranchingBlock(Tree branchingTree, PhpCfgBlock trueSuccessor, PhpCfgBlock falseSuccessor) {
    this.trueSuccessor = trueSuccessor;
    this.falseSuccessor = falseSuccessor;
    this.branchingTree = branchingTree;
  }

  @Override
  public PhpCfgBlock trueSuccessor() {
    return trueSuccessor;
  }

  @Override
  public PhpCfgBlock falseSuccessor() {
    return falseSuccessor;
  }

  @Override
  public Tree branchingTree() {
    return branchingTree;
  }

  @Override
  public ImmutableSet<CfgBlock> successors(){
    return ImmutableSet.of(trueSuccessor, falseSuccessor);
  }

  @Override
  public void replaceSuccessors(Map<PhpCfgBlock, PhpCfgBlock> replacements) {
    this.trueSuccessor = replacement(this.trueSuccessor, replacements);
    this.falseSuccessor = replacement(this.falseSuccessor, replacements);
  }
}
