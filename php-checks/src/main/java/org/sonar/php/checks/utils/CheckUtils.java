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
package org.sonar.php.checks.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
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

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CheckUtils {

  public static final ImmutableMap<String, String> PREDEFINED_VARIABLES = ImmutableMap.<String, String>builder()
    .put("$HTTP_SERVER_VARS", "$_SERVER")
    .put("$HTTP_GET_VARS", "$_GET")
    .put("$HTTP_POST_VARS", "$_POST")
    .put("$HTTP_POST_FILES", "$_FILES")
    .put("$HTTP_SESSION_VARS", "$_SESSION")
    .put("$HTTP_ENV_VARS", "$_ENV")
    .put("$HTTP_COOKIE_VARS", "$_COOKIE").build();

  private CheckUtils() {
  }

  public static boolean isSuperGlobal(String varName) {
    return "$GLOBALS".equals(varName) || PREDEFINED_VARIABLES.values().contains(varName);
  }

  public static boolean hasModifier(List<SyntaxToken> modifiers, String toFind) {
    for (SyntaxToken modifier : modifiers) {
      if (modifier.text().equalsIgnoreCase(toFind)) {
        return true;
      }
    }
    return false;
  }

  public static boolean areSyntacticallyEquivalent(@Nullable Tree tree1, @Nullable Tree tree2) {
    if (tree1 == tree2) {
      return true;
    }

    if (tree1 == null || tree2 == null) {
      return false;
    }

    PHPTree phpTree1 = (PHPTree) tree1;
    PHPTree phpTree2 = (PHPTree) tree2;

    if (phpTree1.getKind() != phpTree2.getKind()) {
      return false;
    } else if (phpTree1.isLeaf()) {
      return phpTree1.getFirstToken().text().equals(phpTree2.getFirstToken().text());
    }

    Iterator<Tree> iterator1 = phpTree1.childrenIterator();
    Iterator<Tree> iterator2 = phpTree2.childrenIterator();
    return areSyntacticallyEquivalent(iterator1, iterator2);
  }

  public static boolean areSyntacticallyEquivalent(Iterator<? extends Tree> iterator1, Iterator<? extends Tree> iterator2) {
    while (iterator1.hasNext() && iterator2.hasNext()) {
      if (!areSyntacticallyEquivalent(iterator1.next(), iterator2.next())) {
        return false;
      }
    }

    return !iterator1.hasNext() && !iterator2.hasNext();
  }

  public static String asString(Tree tree) {
    if (tree.is(Tree.Kind.TOKEN)) {
      return ((SyntaxToken) tree).text();

    } else {
      StringBuilder sb = new StringBuilder();
      Iterator<Tree> treeIterator = ((PHPTree) tree).childrenIterator();
      SyntaxToken prevToken = null;

      while (treeIterator.hasNext()) {
        Tree child = treeIterator.next();

        if (child != null && !child.is(Kind.SKIPPED_LIST_ELEMENT)) {
          appendChild(sb, prevToken, child);
          prevToken = ((PHPTree) child).getLastToken();
        }
      }
      return sb.toString();
    }
  }

  private static void appendChild(StringBuilder sb, @Nullable SyntaxToken prevToken, Tree child) {
    if (prevToken != null) {
      SyntaxToken firstToken = ((PHPTree) child).getFirstToken();
      if (isSpaceRequired(prevToken, firstToken)) {
        sb.append(" ");
      }
    }
    sb.append(asString(child));
  }

  private static boolean isSpaceRequired(SyntaxToken prevToken, SyntaxToken token) {
    return (token.line() > prevToken.line()) || (prevToken.column() + prevToken.text().length() < token.column());
  }
  
  public static final  List<Kind> STATEMENT_CONTAINERS = ImmutableList.of(
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
