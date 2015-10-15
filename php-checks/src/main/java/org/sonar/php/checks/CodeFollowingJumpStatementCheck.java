/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = CodeFollowingJumpStatementCheck.KEY,
  name = "Jump statements should not be followed by other statements",
  priority = Priority.MAJOR,
  tags = {Tags.MISRA, Tags.CERT, Tags.CWE, Tags.UNUSED})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("5min")
public class CodeFollowingJumpStatementCheck extends PHPSubscriptionCheck {

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
    Tree.Kind.USE_CONST_STATEMENT,
    Tree.Kind.USE_STATEMENT,
    Tree.Kind.USE_FUNCTION_STATEMENT,
    Tree.Kind.CONSTANT_DECLARATION
  };

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return CheckUtils.STATEMENT_CONTAINERS;
  }

  @Override
  public void visitNode(Tree tree) {
    List<StatementTree> statements = CheckUtils.getStatements(tree);

    for (int i = 0; i < statements.size() - 1; i++) {
      StatementTree currentStatement = statements.get(i);

      if (currentStatement.is(JUMP_KINDS) && hasActionStatementAfter(statements, i)) {
        String message = String.format(MESSAGE, ((PHPTree) currentStatement).getFirstToken().text());
        context().newIssue(KEY, message).tree(currentStatement);
      }
    }

  }

  private boolean hasActionStatementAfter(List<StatementTree> statements, int currentStatementNumber) {
    for (int i = currentStatementNumber + 1; i < statements.size(); i++) {
      if (!statements.get(i).is(NO_ACTION_KINDS)) {
        return true;
      }
    }
    return false;
  }

}
