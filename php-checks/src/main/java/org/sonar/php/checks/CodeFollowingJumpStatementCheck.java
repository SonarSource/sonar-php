/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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
import org.sonar.php.checks.utils.AbstractStatementsCheck;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;

@Rule(key = CodeFollowingJumpStatementCheck.KEY)
public class CodeFollowingJumpStatementCheck extends AbstractStatementsCheck {

  public static final String KEY = "S1763";
  private static final String MESSAGE = "Remove the code after this \"%s\".";

  private static final Tree.Kind[] JUMP_KINDS = {
    Tree.Kind.BREAK_STATEMENT,
    Tree.Kind.RETURN_STATEMENT,
    Tree.Kind.CONTINUE_STATEMENT,
    Tree.Kind.THROW_STATEMENT
  };

  private static final Tree.Kind[] NO_ACTION_KINDS = {
    Tree.Kind.EMPTY_STATEMENT,
    Tree.Kind.CLASS_DECLARATION,
    Tree.Kind.FUNCTION_DECLARATION,
    Tree.Kind.INTERFACE_DECLARATION,
    Tree.Kind.TRAIT_DECLARATION,
    Tree.Kind.NAMESPACE_STATEMENT,
    Tree.Kind.USE_STATEMENT,
    Tree.Kind.CONSTANT_DECLARATION
  };

  @Override
  public void visitNode(Tree tree) {
    List<StatementTree> statements = getStatements(tree);

    for (int i = 0; i < statements.size() - 1; i++) {
      StatementTree currentStatement = statements.get(i);

      if (currentStatement.is(JUMP_KINDS) && hasActionStatementAfter(statements, i)) {
        String message = String.format(MESSAGE, ((PHPTree) currentStatement).getFirstToken().text());
        context().newIssue(this, ((PHPTree) currentStatement).getFirstToken(), message);
      }
    }

  }

  private static boolean hasActionStatementAfter(List<StatementTree> statements, int currentStatementNumber) {
    for (int i = currentStatementNumber + 1; i < statements.size(); i++) {
      if (!statements.get(i).is(NO_ACTION_KINDS)) {
        return true;
      }
    }
    return false;
  }

}
