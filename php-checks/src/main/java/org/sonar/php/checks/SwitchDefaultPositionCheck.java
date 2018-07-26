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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.plugins.php.api.tree.Tree.Kind;

@Rule(key = SwitchDefaultPositionCheck.KEY)
public class SwitchDefaultPositionCheck extends PHPVisitorCheck {

  public static final String KEY = "S4524";

  private static final String MESSAGE = "Move this \"default\" clause to the beginning or end of this \"switch\" statement.";

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    List<SwitchCaseClauseTree> clauses = tree.cases();
    if (clauses.size() > 2) {
      clauses.subList(1, clauses.size() - 1)
        .stream()
        .filter(clause -> clause.is(Kind.DEFAULT_CLAUSE))
        .forEach(defaultTree -> context().newIssue(this, defaultTree.caseToken(), MESSAGE));
    }
    super.visitSwitchStatement(tree);
  }

}
