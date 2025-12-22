/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.Locale;
import org.sonar.check.Rule;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = MethodNameReturningBooleanCheck.KEY)
public class MethodNameReturningBooleanCheck extends PHPVisitorCheck {

  public static final String KEY = "S2047";
  private static final String MESSAGE = "Rename this method to start with \"is\" or \"has\".";

  private static final String RETURN_TAG = "@return";

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    super.visitMethodDeclaration(tree);

    if (isReturningBoolean(tree) && !hasBooleanPrefixName(tree)) {
      context().newIssue(this, tree.name(), MESSAGE);
    }
  }

  private static boolean hasBooleanPrefixName(MethodDeclarationTree methodDeclaration) {
    String methodName = methodDeclaration.name().text();
    return methodName.startsWith("has") || methodName.startsWith("is");
  }

  private static boolean isReturningBoolean(MethodDeclarationTree methodDeclaration) {
    for (SyntaxTrivia comment : ((PHPTree) methodDeclaration).getFirstToken().trivias()) {
      for (String line : comment.text().split(LexicalConstant.LINE_TERMINATOR)) {

        if (line.toLowerCase(Locale.ROOT).contains(RETURN_TAG)) {

          return returnsBoolean(line);
        }
      }
    }
    return false;
  }

  private static boolean returnsBoolean(String line) {
    boolean isPreviousReturnTag = false;

    for (String word : line.split("\\s")) {
      String s = word.trim();

      if (RETURN_TAG.equals(s)) {
        isPreviousReturnTag = true;

      } else if (isPreviousReturnTag) {
        return "bool".equalsIgnoreCase(s) || "boolean".equalsIgnoreCase(s);
      }
    }
    return false;
  }

}
