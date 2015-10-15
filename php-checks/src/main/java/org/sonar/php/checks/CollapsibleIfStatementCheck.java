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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import javax.annotation.Nullable;

@Rule(
  key = CollapsibleIfStatementCheck.KEY,
  name = "Collapsible \"if\" statements should be merged",
  priority = Priority.MAJOR,
  tags = Tags.CLUMSY)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("5min")
public class CollapsibleIfStatementCheck extends PHPVisitorCheck {

  public static final String KEY = "S1066";
  private static final String MESSAGE = "Merge this if statement with the enclosing one.";

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    super.visitIfStatement(tree);

    if (!hasElseOrElseIf(tree)) {
      StatementTree singleStatement = getSingleNestedStatement(tree);

      if (singleStatement != null && isIfStatementWithoutElse(singleStatement)) {
        context().newIssue(KEY, MESSAGE).tree(singleStatement);
      }
    }
  }

  private static boolean isIfStatementWithoutElse(StatementTree statement) {
    return statement.is(Kind.IF_STATEMENT, Kind.ALTERNATIVE_IF_STATEMENT) && !hasElseOrElseIf((IfStatementTree) statement);
  }

  @Nullable
  private static StatementTree getSingleNestedStatement(IfStatementTree ifStatement) {
    if (ifStatement.statements().size() == 1) {
      StatementTree statement = ifStatement.statements().get(0);

      if (statement.is(Kind.BLOCK)) {
        BlockTree blockTree = (BlockTree) statement;

        if (blockTree.statements().size() == 1) {
          return blockTree.statements().get(0);
        }

      } else {
        return statement;
      }
    }

    return null;
  }

  private static boolean hasElseOrElseIf(IfStatementTree ifStatement) {
    return ifStatement.elseClause() != null || !ifStatement.elseifClauses().isEmpty();
  }


}
