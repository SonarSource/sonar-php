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

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.metrics.CognitiveComplexityVisitor;
import org.sonar.php.metrics.CognitiveComplexityVisitor.CognitiveComplexity;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = FunctionCognitiveComplexityCheck.KEY)
public class FunctionCognitiveComplexityCheck extends PHPVisitorCheck {

  public static final String KEY = "S3776";

  private static final String MESSAGE = "Refactor this function to reduce its Cognitive Complexity from %s to the %s allowed.";

  public static final int DEFAULT = 15;

  @RuleProperty(
    key = "threshold",
    description = "The maximum authorized complexity.",
    defaultValue = "" + DEFAULT)
  private int threshold = DEFAULT;

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    checkFunctionComplexity(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    checkFunctionComplexity(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    checkFunctionComplexity(tree);
  }

  private void checkFunctionComplexity(FunctionTree functionTree) {
    CognitiveComplexity complexity = CognitiveComplexityVisitor.complexity(functionTree);

    if (complexity.getValue() > threshold) {
      String message = String.format(MESSAGE, complexity.getValue(), threshold);
      int cost = complexity.getValue() - threshold;
      PreciseIssue issue = context().newIssue(this, (functionTree).functionToken(), message).cost(cost);

      complexity.getComplexityComponents().forEach(complexityComponent ->
        issue.secondary(
          complexityComponent.tree(),
          secondaryMessage(complexityComponent.addedComplexity())));
    }
  }

  private static String secondaryMessage(int complexity) {
    if (complexity == 1) {
      return "+1";

    } else{
      return String.format("+%s (incl. %s for nesting)", complexity, complexity - 1);
    }
  }

  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

}
