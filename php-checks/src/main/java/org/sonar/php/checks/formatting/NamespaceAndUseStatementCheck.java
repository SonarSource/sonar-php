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
package org.sonar.php.checks.formatting;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.sonar.php.checks.FormattingStandardCheck;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.List;

public class NamespaceAndUseStatementCheck extends PHPVisitorCheck implements FormattingCheck {

  private static final String BLANK_LINE_NAMESPACE_MESSAGE = "Add a blank line after this \"namespace%s\" declaration.";
  private static final String BLANK_LINE_USE_MESSAGE = "Add a blank line after this \"use\" declaration.";
  private static final String USE_AFTER_NAMESPACE_MESSAGE = "Move the use declarations after the namespace declarations.";

  private List<UseStatementTree> useStatements = Lists.newArrayList();
  private StatementTree nextStatement = null;
  private FormattingStandardCheck check = null;


  @Override
  public void checkFormat(FormattingStandardCheck formattingCheck, ScriptTree scriptTree) {
    this.check = formattingCheck;

    this.visitScript(scriptTree);

    nextStatement = null;
    useStatements.clear();
  }

  @Override
  public void visitScript(ScriptTree tree) {
    List<StatementTree> statements = tree.statements();
    int nbStatements = statements.size();

    for (int i = 0; i < nbStatements - 1; i++) {
      nextStatement = statements.get(i + 1);
      statements.get(i).accept(this);
    }
  }

  @Override
  public void visitNamespaceStatement(NamespaceStatementTree tree) {
    if (check.hasNamespaceBlankLine && !isFollowedWithBlankLine(tree)) {
      String message = String.format(
        BLANK_LINE_NAMESPACE_MESSAGE,
        tree.namespaceName() == null ? "" : (" " + tree.namespaceName().fullName()));
      reportIssue(message, tree.namespaceToken());
    }
  }

  @Override
  public void visitUseStatement(UseStatementTree tree) {
    useStatements.add(tree);

    if (!nextStatement.is(Kind.USE_STATEMENT)) {
      checkUsesAreBeforeNamespace();
      checkBlankLineAfterUses(tree);
      useStatements.clear();
    }
  }

  private void checkBlankLineAfterUses(UseStatementTree useStatement) {
    if (check.hasUseBlankLine && !isFollowedWithBlankLine(useStatement)) {
      reportIssue(BLANK_LINE_USE_MESSAGE, Iterables.getLast(useStatements).useToken());
    }
  }

  private void checkUsesAreBeforeNamespace() {
    if (check.isUseAfterNamespace && nextStatement.is(Kind.NAMESPACE_STATEMENT)) {
      reportIssue(USE_AFTER_NAMESPACE_MESSAGE, useStatements.get(0).useToken());
    }
  }

  private void reportIssue(String message, Tree tree) {
    check.reportIssue(message, tree);
  }

  /**
   * Returns true when there is either token or comment on node's next line.
   */
  private boolean isFollowedWithBlankLine(Tree tree) {
    int nextLine = ((PHPTree) tree).getLastToken().line() + 1;
    SyntaxToken nextToken = ((PHPTree) nextStatement).getFirstToken();
    boolean isFollowedWithBlankLine = true;

    // Checking for comment: is allowed on the same line as the node declaration or on next line + 1.
    for (SyntaxTrivia trivia : nextToken.trivias()) {
      isFollowedWithBlankLine &= trivia.line() != nextLine;
    }

    return isFollowedWithBlankLine && nextToken.line() != nextLine;
  }

}
