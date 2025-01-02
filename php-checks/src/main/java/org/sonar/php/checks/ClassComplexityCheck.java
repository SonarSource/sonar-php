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
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.metrics.ComplexityVisitor;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = ClassComplexityCheck.KEY)
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
    return Arrays.asList(Kind.CLASS_DECLARATION, Kind.ANONYMOUS_CLASS);
  }

  @Override
  public void visitNode(Tree tree) {
    List<Tree> complexityTrees = ComplexityVisitor.complexityTrees(tree);
    int complexity = complexityTrees.size();
    if (complexity > max) {
      int cost = complexity - max;
      PreciseIssue issue = context().newIssue(this, ((ClassTree) tree).classToken(), message(tree, complexity)).cost(cost);
      complexityTrees.forEach(complexityTree -> issue.secondary(complexityTree, "+1"));
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
