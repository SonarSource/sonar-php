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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.metrics.ComplexityVisitor;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = "S1541")
public class FunctionComplexityCheck extends PHPSubscriptionCheck {

  private static final String MESSAGE = "The Cyclomatic Complexity of this function %s is %s which is greater than %s authorized.";

  public static final int DEFAULT = 20;

  @RuleProperty(
    key = "threshold",
    defaultValue = "" + DEFAULT)
  int threshold = DEFAULT;

  @Override
  public List<Kind> nodesToVisit() {
    return CheckUtils.getFunctionKinds();
  }

  @Override
  public void visitNode(Tree tree) {
    List<Tree> complexityTrees = ComplexityVisitor.complexityNodesWithoutNestedFunctions(tree);
    int complexity = complexityTrees.size();
    if (complexity > threshold) {
      String functionName = CheckUtils.getFunctionName((FunctionTree) tree);
      String message = String.format(MESSAGE, functionName, complexity, threshold);
      int cost = complexity - threshold;
      PreciseIssue issue = context().newIssue(this, ((FunctionTree) tree).functionToken(), message).cost(cost);
      complexityTrees.forEach(complexityTree -> issue.secondary(complexityTree, "+1"));
    }
  }

}
