/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.api.cfg;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.sonar.plugins.php.api.tree.Tree;

class PhpCfgBranchingBlock extends PhpCfgBlock implements CfgBranchingBlock {

  private CfgBlock trueSuccessor;
  private CfgBlock falseSuccessor;

  private Tree branchingTree;

  public PhpCfgBranchingBlock(Tree branchingTree, PhpCfgBlock trueSuccessor, PhpCfgBlock falseSuccessor) {
    this.trueSuccessor = trueSuccessor;
    this.falseSuccessor = falseSuccessor;
    this.branchingTree = branchingTree;
  }

  @Override
  public CfgBlock trueSuccessor() {
    return trueSuccessor;
  }

  @Override
  public CfgBlock falseSuccessor() {
    return falseSuccessor;
  }

  @Override
  public Tree branchingTree() {
    return branchingTree;
  }

  @Override
  public Set<CfgBlock> successors() {
    // `trueSuccessor` and `falseSuccessor` can be equal, but to comply with the API we need to have them in a Set
    return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(trueSuccessor, falseSuccessor)));
  }

  @Override
  public void replaceSuccessors(Map<PhpCfgBlock, PhpCfgBlock> replacements) {
    this.trueSuccessor = replacement(this.trueSuccessor, replacements);
    this.falseSuccessor = replacement(this.falseSuccessor, replacements);
  }
}
