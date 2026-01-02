/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

public class AbstractStatementsCheck extends PHPSubscriptionCheck {

  @Override
  public List<Kind> nodesToVisit() {
    return Arrays.asList(
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
    return switch (tree.getKind()) {
      case SCRIPT -> ((ScriptTree) tree).statements();
      case BLOCK -> ((BlockTree) tree).statements();
      case CASE_CLAUSE, DEFAULT_CLAUSE -> ((SwitchCaseClauseTree) tree).statements();
      case DECLARE_STATEMENT -> ((DeclareStatementTree) tree).statements();
      case IF_STATEMENT, ALTERNATIVE_IF_STATEMENT -> ((IfStatementTree) tree).statements();
      case ELSE_CLAUSE, ALTERNATIVE_ELSE_CLAUSE -> ((ElseClauseTree) tree).statements();
      case ELSEIF_CLAUSE, ALTERNATIVE_ELSEIF_CLAUSE -> ((ElseifClauseTree) tree).statements();
      case FOREACH_STATEMENT, ALTERNATIVE_FOREACH_STATEMENT -> ((ForEachStatementTree) tree).statements();
      case FOR_STATEMENT, ALTERNATIVE_FOR_STATEMENT -> ((ForStatementTree) tree).statements();
      case NAMESPACE_STATEMENT -> ((NamespaceStatementTree) tree).statements();
      case WHILE_STATEMENT -> ((WhileStatementTree) tree).statements();
      default -> Collections.emptyList();
    };
  }

}
