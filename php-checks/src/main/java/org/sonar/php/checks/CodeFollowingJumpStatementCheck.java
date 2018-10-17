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
import com.sonar.sslr.api.RecognitionException;
import java.util.List;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Rule;
import org.sonar.php.cfg.CfgBlock;
import org.sonar.php.cfg.ControlFlowGraph;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = CodeFollowingJumpStatementCheck.KEY)
public class CodeFollowingJumpStatementCheck extends PHPSubscriptionCheck {

  private static final Logger LOG = Loggers.get(CodeFollowingJumpStatementCheck.class);

  public static final String KEY = "S1763";
  private static final String MESSAGE = "Remove this unreachable code.";

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.<Kind>builder()
      .addAll(CheckUtils.FUNCTION_KINDS)
      .add(Kind.SCRIPT)
      .build();
  }

  @Override
  public void visitNode(Tree tree) {
    ControlFlowGraph cfg = null;
    try {
      switch (tree.getKind()) {
        case METHOD_DECLARATION:
          if (((MethodDeclarationTree) tree).body().is(Kind.BLOCK)) {
            cfg = ControlFlowGraph.build((BlockTree) ((MethodDeclarationTree) tree).body());
          }
          break;
        case FUNCTION_DECLARATION:
          cfg = ControlFlowGraph.build(((FunctionDeclarationTree) tree).body());
          break;
        case FUNCTION_EXPRESSION:
          cfg = ControlFlowGraph.build(((FunctionExpressionTree) tree).body());
          break;
        case SCRIPT:
          cfg = ControlFlowGraph.build((ScriptTree) tree);
          break;
        default:
          throw new IllegalStateException("Unexpected tree kind " + tree.getKind());
      }
    } catch (RecognitionException e) {
      LOG.warn("[Rule {}] Failed to build control flow graph for file [{}] at line {}",
        KEY,
        context().getPhpFile().toString(),
        e.getLine());
    }

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
