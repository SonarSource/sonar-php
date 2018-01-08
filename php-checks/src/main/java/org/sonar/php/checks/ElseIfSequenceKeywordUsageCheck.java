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

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ElseIfSequenceKeywordUsageCheck.KEY)
public class ElseIfSequenceKeywordUsageCheck extends PHPVisitorCheck {

  public static final String KEY = "S1793";
  private static final String MESSAGE = "Replace this \"else if\" keyword sequence by \"elseif\" keyword.";

  @Override
  public void visitElseClause(ElseClauseTree tree) {
    if (!tree.is(Kind.ALTERNATIVE_ELSE_CLAUSE) && isElseIf(tree)) {
      context().newIssue(this, tree.elseToken(), ((IfStatementTree) tree.statements().get(0)).ifToken(), MESSAGE);
    }
    super.visitElseClause(tree);
  }

  private static boolean isElseIf(ElseClauseTree elseClause) {
    return !elseClause.statements().isEmpty() && elseClause.statements().get(0).is(Kind.IF_STATEMENT);
  }

}
