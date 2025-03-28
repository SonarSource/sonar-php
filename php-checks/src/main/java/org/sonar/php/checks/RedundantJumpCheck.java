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
package org.sonar.php.checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.tree.TreeUtils;
import org.sonar.plugins.php.api.cfg.CfgBlock;
import org.sonar.plugins.php.api.cfg.ControlFlowGraph;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

import static org.sonar.php.utils.collections.ListUtils.getLast;
import static org.sonar.plugins.php.api.cfg.ControlFlowGraph.KINDS_WITH_CONTROL_FLOW;

@Rule(key = RedundantJumpCheck.KEY)
public class RedundantJumpCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S3626";
  private static final String MESSAGE = "Remove this redundant jump.";

  @Override
  public List<Kind> nodesToVisit() {
    return new ArrayList<>(KINDS_WITH_CONTROL_FLOW);
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
      if (cfgBlock.successors().size() == 1 && cfgBlock.successors().contains(cfgBlock.syntacticSuccessor())) {
        Tree lastElement = getLast(cfgBlock.elements());
        if (isIgnoredJump(lastElement)) {
          continue;
        }

        if (lastElement.is(Kind.RETURN_STATEMENT, Kind.CONTINUE_STATEMENT, Kind.GOTO_STATEMENT)) {
          context().newIssue(this, lastElement, MESSAGE);
        }
      }
    }
  }

  private static boolean isIgnoredJump(Tree tree) {
    if (tree.is(Kind.RETURN_STATEMENT)
      && (((ReturnStatementTree) tree).expression() != null
        || tree.getParent().is(Kind.CASE_CLAUSE, Kind.DEFAULT_CLAUSE))) {
      return true;
    }

    // ignore jumps in try statement because CFG is not precise
    Tree tryAncestor = TreeUtils.findAncestorWithKind(tree, Collections.singletonList(Kind.TRY_STATEMENT));
    return tryAncestor != null;
  }
}
