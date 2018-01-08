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

import org.apache.commons.lang.StringUtils;
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

        if (StringUtils.containsIgnoreCase(line, RETURN_TAG)) {
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
