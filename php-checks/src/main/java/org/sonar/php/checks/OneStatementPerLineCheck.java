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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Rule(
  key = OneStatementPerLineCheck.KEY,
  name = "Statements should be on separate lines",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION, Tags.PSR2})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("1min")
public class OneStatementPerLineCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S122";
  private static final String MESSAGE = "%s statements were found on this line. Reformat the code to have only one statement per line.";

  private final Map<Integer, StatementCount> statementsPerLine = Maps.newHashMap();
  private final Set<Integer> linesWithHtml = Sets.newHashSet();
  private boolean inFunctionExpression = false;

  private static class StatementCount {
    int nbStatement = 0;
    int nbFunctionExpression = 0;
    int nbNestedStatement = 0;
  }

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(
        Kind.SCRIPT,
        Kind.DECLARE_STATEMENT,
        Kind.IF_STATEMENT,
        Kind.ALTERNATIVE_IF_STATEMENT,
        Kind.FOREACH_STATEMENT,
        Kind.ALTERNATIVE_FOREACH_STATEMENT,
        Kind.FOR_STATEMENT,
        Kind.ALTERNATIVE_FOR_STATEMENT,
        Kind.NAMESPACE_STATEMENT,
        Kind.WHILE_STATEMENT,
        Kind.BREAK_STATEMENT,
        Kind.CLASS_DECLARATION,
        Kind.TRAIT_DECLARATION,
        Kind.INTERFACE_DECLARATION,
        Kind.CONSTANT_DECLARATION,
        Kind.CONTINUE_STATEMENT,
        Kind.DO_WHILE_STATEMENT,
        Kind.EXPRESSION_STATEMENT,
        Kind.FUNCTION_DECLARATION,
        Kind.FUNCTION_EXPRESSION,
        Kind.GLOBAL_STATEMENT,
        Kind.GOTO_STATEMENT,
        Kind.INLINE_HTML,
        Kind.RETURN_STATEMENT,
        Kind.STATIC_STATEMENT,
        Kind.SWITCH_STATEMENT,
        Kind.THROW_STATEMENT,
        Kind.TRY_STATEMENT,
        Kind.UNSET_VARIABLE_STATEMENT,
        Kind.USE_CONST_STATEMENT,
        Kind.USE_FUNCTION_STATEMENT,
        Kind.USE_STATEMENT,
        Kind.YIELD_STATEMENT
    );
  }

  @Override
  public void visitNode(Tree tree) {
    if (tree.is(Kind.SCRIPT)) {
      statementsPerLine.clear();
      linesWithHtml.clear();
      inFunctionExpression = false;
      return;
    }

    int line = line(tree);

    if (tree.is(Kind.INLINE_HTML)) {
      linesWithHtml.add(line);

    } else if (tree.is(Kind.FUNCTION_EXPRESSION)) {

      if (statementsPerLine.containsKey(line)) {
        statementsPerLine.get(line).nbFunctionExpression++;
        inFunctionExpression = true;
      }

    } else {

      if (!statementsPerLine.containsKey(line)) {
        statementsPerLine.put(line, new StatementCount());

      } else if (inFunctionExpression) {
        statementsPerLine.get(line).nbNestedStatement++;
      }

      if (!inFunctionExpression) {
        statementsPerLine.get(line).nbStatement++;
      }
    }
  }

  @Override
  public void leaveNode(Tree tree) {
    if (tree.is(Tree.Kind.FUNCTION_EXPRESSION) && statementsPerLine.containsKey(line(tree))) {
      inFunctionExpression = false;
    }

    if (tree.is(Tree.Kind.SCRIPT)) {
      finish();
    }

  }

  private void finish() {
    for (Map.Entry<Integer, StatementCount> statementsAtLine : statementsPerLine.entrySet()) {
      Integer line = statementsAtLine.getKey();
      if (linesWithHtml.contains(line)) {
        continue;
      }

      StatementCount stmtCount = statementsAtLine.getValue();

      if (stmtCount.nbStatement > 1 || stmtCount.nbFunctionExpression > 1 || stmtCount.nbNestedStatement > 1) {
        String message = String.format(MESSAGE, stmtCount.nbStatement + stmtCount.nbNestedStatement);
        context().newIssue(KEY, message).line(line);
      }
    }
  }

  private static int line(Tree tree) {
    return ((PHPTree) tree).getLine();
  }
}
