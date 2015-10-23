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
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.FunctionUtils;
import org.sonar.php.metrics.NewComplexityVisitor;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.squidbridge.annotations.SqaleLinearWithOffsetRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = FunctionComplexityCheck.KEY,
  name = "Functions should not be too complex",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNIT_TESTABILITY)
@SqaleLinearWithOffsetRemediation(coeff = "1min", offset = "10min", effortToFixDescription = "per complexity point above the threshold")
public class FunctionComplexityCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S1541";

  private static final String MESSAGE = "The Cyclomatic Complexity of this function %s is %s which is greater than %s authorized.";

  public static final int DEFAULT = 20;

  @RuleProperty(
    key = "threshold",
    defaultValue = "" + DEFAULT)
  int threshold = DEFAULT;

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(
      Kind.FUNCTION_DECLARATION,
      Kind.METHOD_DECLARATION,
      Kind.FUNCTION_EXPRESSION);
  }

  @Override
  public void visitNode(Tree tree) {
    int complexity = NewComplexityVisitor.complexityWithoutNestedFunctions(tree);
    if (complexity > threshold) {
      String functionName = FunctionUtils.getFunctionName((FunctionTree) tree);
      String message = String.format(MESSAGE, functionName, complexity, threshold);
      int cost = complexity - threshold;
      context().newIssue(KEY, message).tree(tree).cost(cost);
    }
  }

}
