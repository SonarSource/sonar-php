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
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.metrics.NewComplexityVisitor;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.squidbridge.annotations.SqaleLinearWithOffsetRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = ClassComplexityCheck.KEY,
  name = "Classes should not be too complex",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleLinearWithOffsetRemediation(coeff = "1min", offset = "10min", effortToFixDescription = "per complexity point over the threshold")
public class ClassComplexityCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S1311";

  private static final String MESSAGE = "The Cyclomatic Complexity of this class \"%s\" is %s which is greater than %s authorized, split this class.";

  public static final int DEFAULT = 200;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  int max = DEFAULT;

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(Kind.CLASS_DECLARATION);
  }

  @Override
  public void visitNode(Tree tree) {
    int complexity = NewComplexityVisitor.complexity(tree);
    if (complexity > max) {
      String className = ((ClassDeclarationTree) tree).name().text();
      String message = String.format(MESSAGE, className, complexity, max);
      int cost = complexity - max;
      context().newIssue(KEY, message).tree(tree).cost(cost);
    }
  }

}
