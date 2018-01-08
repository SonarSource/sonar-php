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
package org.sonar.php.checks.utils;

import com.google.common.collect.ImmutableList;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.DeclareStatementTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

import java.util.Collections;
import java.util.List;

public class AbstractStatementsCheck extends PHPSubscriptionCheck {

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(
      Kind.SCRIPT,
      Kind.BLOCK,
      Kind.CASE_CLAUSE,
      Kind.DEFAULT_CLAUSE,
      Kind.DECLARE_STATEMENT,
      Kind.IF_STATEMENT,
      Kind.ALTERNATIVE_IF_STATEMENT,
      Kind.ELSE_CLAUSE,
      Kind.ALTERNATIVE_ELSE_CLAUSE,
      Kind.ELSEIF_CLAUSE,
      Kind.ALTERNATIVE_ELSEIF_CLAUSE,
      Kind.FOREACH_STATEMENT,
      Kind.ALTERNATIVE_FOREACH_STATEMENT,
      Kind.FOR_STATEMENT,
      Kind.ALTERNATIVE_FOR_STATEMENT,
      Kind.NAMESPACE_STATEMENT,
      Kind.WHILE_STATEMENT);
  }

  public static List<StatementTree> getStatements(Tree tree) {
    List<StatementTree> statements = Collections.emptyList();
    switch (tree.getKind()) {
      case SCRIPT:
        statements = ((ScriptTree) tree).statements();
        break;
      case BLOCK:
        statements = ((BlockTree) tree).statements();
        break;
      case CASE_CLAUSE:
      case DEFAULT_CLAUSE:
        statements = ((SwitchCaseClauseTree) tree).statements();
        break;
      case DECLARE_STATEMENT:
        statements = ((DeclareStatementTree) tree).statements();
        break;
      case IF_STATEMENT:
      case ALTERNATIVE_IF_STATEMENT:
        statements = ((IfStatementTree) tree).statements();
        break;
      case ELSE_CLAUSE:
      case ALTERNATIVE_ELSE_CLAUSE:
        statements = ((ElseClauseTree) tree).statements();
        break;
      case ELSEIF_CLAUSE:
      case ALTERNATIVE_ELSEIF_CLAUSE:
        statements = ((ElseifClauseTree) tree).statements();
        break;
      case FOREACH_STATEMENT:
      case ALTERNATIVE_FOREACH_STATEMENT:
        statements = ((ForEachStatementTree) tree).statements();
        break;
      case FOR_STATEMENT:
      case ALTERNATIVE_FOR_STATEMENT:
        statements = ((ForStatementTree) tree).statements();
        break;
      case NAMESPACE_STATEMENT:
        statements = ((NamespaceStatementTree) tree).statements();
        break;
      case WHILE_STATEMENT:
        statements = ((WhileStatementTree) tree).statements();
        break;
      default:
        break;
    }
    return statements;
  }

}
