/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = "S122")
public class OneStatementPerLineCheck extends PHPSubscriptionCheck {
  private static final String MESSAGE = "%s %s were found on this line. Reformat the code to have only one %s per line.";

  private final Map<Integer, StatementCount> statementsPerLine = new HashMap<>();
  private final Set<Integer> linesWithHtml = new HashSet<>();
  private boolean inFunctionExpression = false;

  private static class StatementCount {
    int nbStatement = 0;
    int nbFunctionExpression = 0;
    int nbNestedStatement = 0;
  }

  @Override
  public List<Kind> nodesToVisit() {
    return Arrays.asList(
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
      Kind.INLINE_HTML_TOKEN,
      Kind.RETURN_STATEMENT,
      Kind.STATIC_STATEMENT,
      Kind.SWITCH_STATEMENT,
      Kind.THROW_STATEMENT,
      Kind.TRY_STATEMENT,
      Kind.UNSET_VARIABLE_STATEMENT,
      Kind.USE_STATEMENT);
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

    if (tree.is(Kind.INLINE_HTML_TOKEN)) {
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

      if (stmtCount.nbStatement > 1 || stmtCount.nbNestedStatement > 1) {
        reportIssue(line, stmtCount.nbStatement + stmtCount.nbNestedStatement, "statement");
      } else if (stmtCount.nbFunctionExpression > 1) {
        reportIssue(line, stmtCount.nbFunctionExpression, "function expression");
      }
    }
  }

  private void reportIssue(int line, int actualCount, String name) {
    context().newLineIssue(this, line, MESSAGE.formatted(actualCount, name + "s", name));
  }

  private static int line(Tree tree) {
    return ((PHPTree) tree).getLine();
  }
}
