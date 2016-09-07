/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.metrics.ComplexityVisitor;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.squidbridge.annotations.SqaleLinearWithOffsetRemediation;

@Rule(
  key = ClassComplexityCheck.KEY,
  name = "Classes should not be too complex",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@SqaleLinearWithOffsetRemediation(coeff = "1min", offset = "10min", effortToFixDescription = "per complexity point over the threshold")
public class ClassComplexityCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S1311";

  private static final String MESSAGE = "The Cyclomatic Complexity of this class \"%s\" is %s which is greater than %s authorized, split this class.";
  private static final String MESSAGE_ANONYMOUS_CLASS = "The Cyclomatic Complexity of this anonymous class is %s which is greater than %s authorized, split this class.";

  public static final int DEFAULT = 200;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  int max = DEFAULT;

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(Kind.CLASS_DECLARATION, Kind.ANONYMOUS_CLASS);
  }

  @Override
  public void visitNode(Tree tree) {
    int complexity = ComplexityVisitor.complexity(tree);
    if (complexity > max) {
      int cost = complexity - max;
      context().newIssue(this, message(tree, complexity)).tree(tree).cost(cost);
    }
  }

  private String message(Tree tree, int complexity) {
    if (tree.is(Kind.CLASS_DECLARATION)) {
      String className = ((ClassDeclarationTree) tree).name().text();
      return String.format(MESSAGE, className, complexity, max);

    } else {
      return String.format(MESSAGE_ANONYMOUS_CLASS, complexity, max);
    }
  }

}
