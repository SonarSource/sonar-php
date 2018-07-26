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
import org.sonar.php.checks.utils.AbstractDuplicateBranchImplementationCheck;
import org.sonar.php.checks.utils.SyntacticEquivalence;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.StatementTree;

@Rule(key = DuplicateBranchImplementationCheck.KEY)
public class DuplicateBranchImplementationCheck extends AbstractDuplicateBranchImplementationCheck {

  public static final String KEY = "S1871";
  private static final String MESSAGE = "This %s's code block is the same as the block for the %s on line %s.";

  @Override
  protected void onAllEquivalentBranches(SyntaxToken keyword, List<List<StatementTree>> branches, boolean hasDefault, boolean hasFallthrough) {
    if (!hasDefault) {
      branches.stream().skip(1).forEach(branch -> raiseIssue("branch", branches.get(0), branch));
    }
    // otherwise do nothing, case handled by S3923
  }

  @Override
  protected void checkForDuplication(String branchType, List<List<StatementTree>> list) {
    for (int i = 1; i < list.size(); i++) {
      for (int j = 0; j < i; j++) {
        if (areSyntacticallyEquivalent(list.get(i), list.get(j))) {
          raiseIssue(branchType, list.get(j), list.get(i));
          break;
        }
      }
    }
  }

  private static boolean areSyntacticallyEquivalent(List<StatementTree> list1, List<StatementTree> list2) {
    boolean bothEmpty = list1.isEmpty() && list2.isEmpty();
    return !bothEmpty && SyntacticEquivalence.areSyntacticallyEquivalent(list1.iterator(), list2.iterator());
  }

  private void raiseIssue(String branchType, List<StatementTree> duplicatedTree, List<StatementTree> duplicatingTree) {
    String message = String.format(MESSAGE, branchType, branchType, ((PHPTree) duplicatedTree.get(0)).getLine());
    context()
      .newIssue(this, duplicatingTree.get(0), duplicatingTree.get(duplicatingTree.size() - 1), message)
      .secondary(duplicatedTree.get(0), duplicatedTree.get(duplicatedTree.size() - 1), null);
  }
}
