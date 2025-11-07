/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.sonar.plugins.php.api.tree.Tree;

/**
 * The end node of a {@link ControlFlowGraph}.
 */
class PhpCfgEndBlock extends PhpCfgBlock {

  @Override
  public Set<CfgBlock> successors() {
    return Collections.emptySet();
  }

  @Override
  public void addElement(Tree element) {
    throw new UnsupportedOperationException("Cannot add element to end block");
  }

  @Override
  public void replaceSuccessors(Map<PhpCfgBlock, PhpCfgBlock> replacements) {
    throw new UnsupportedOperationException("Cannot replace successors of end block");
  }

  @Override
  public String toString() {
    return "END";
  }
}
