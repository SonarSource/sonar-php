/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.metrics.LineVisitor;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = TooManyLinesInClassCheck.KEY)
public class TooManyLinesInClassCheck extends PHPVisitorCheck {

  public static final String KEY = "S2042";

  private static final String MESSAGE = "Class \"%s\" has %s lines, which is greater than the %s authorized. Split it into smaller classes.";
  private static final String MESSAGE_ANONYMOUS_CLASS = "This anonymous class has %s lines, which is greater than the %s authorized. Split it into smaller classes.";

  private static final int DEFAULT = 200;

  @RuleProperty(
    key = "maximumLinesThreshold",
    description = "The maximum number of lines of code",
    defaultValue = "" + DEFAULT)
  public int maximumLinesThreshold = DEFAULT;

  @Override
  public void visitClassDeclaration(ClassDeclarationTree declaration) {
    checkClass(declaration);
    super.visitClassDeclaration(declaration);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    checkClass(tree);
    super.visitAnonymousClass(tree);
  }

  private void checkClass(ClassTree tree) {
    int numberOfLines = LineVisitor.linesOfCode(tree);
    if (numberOfLines > maximumLinesThreshold) {

      String message;
      if (tree.is(Tree.Kind.ANONYMOUS_CLASS)) {
        message = String.format(MESSAGE_ANONYMOUS_CLASS, numberOfLines, maximumLinesThreshold);
      } else {
        message = String.format(MESSAGE, ((ClassDeclarationTree) tree).name().text(), numberOfLines, maximumLinesThreshold);
      }
      context().newIssue(this, tree.classToken(), message);
    }
  }
}
