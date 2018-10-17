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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.cfg.CfgBlock;
import org.sonar.php.cfg.ControlFlowGraph;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = CodeFollowingJumpStatementCheck.KEY)
public class CodeFollowingJumpStatementCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S1763";
  private static final String MESSAGE = "Remove this unreachable code.";

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.copyOf(ControlFlowGraph.KINDS_WITH_CONTROL_FLOW);
  }

  @Override
  public void visitNode(Tree tree) {
    ControlFlowGraph cfg = ControlFlowGraph.build(tree, context());
    if (cfg != null) {
      checkCfg(cfg);
    }
  }

  private void checkCfg(ControlFlowGraph cfg) {
    for (CfgBlock cfgBlock : cfg.blocks()) {
      if (cfgBlock.predecessors().isEmpty() && !cfgBlock.equals(cfg.start()) && !cfgBlock.elements().isEmpty()) {
        Tree firstElement = cfgBlock.elements().get(0);
        if (firstElement.is(Kind.BREAK_STATEMENT) && firstElement.getParent().is(Kind.CASE_CLAUSE, Kind.DEFAULT_CLAUSE)) {
          continue;
        }

        PreciseIssue issue = context().newIssue(this, firstElement, MESSAGE);
        cfg.blocks().stream()
          .filter(block -> cfgBlock.equals(block.syntacticSuccessor()))
          .forEach(block -> issue.secondary(block.elements().get(block.elements().size() - 1), null));
      }
    }
  }
}
