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
package org.sonar.php.checks.formatting;

import org.sonar.php.checks.FormattingStandardCheck;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import javax.annotation.Nullable;

public class ExtendsImplementsLineCheck extends PHPVisitorCheck implements FormattingCheck {

  private static final String MESSAGE = "Move %s to the same line as the declaration of its class name, \"%s\".";
  private FormattingStandardCheck check;

  @Override
  public void checkFormat(FormattingStandardCheck formattingCheck, ScriptTree scriptTree) {
    this.check = formattingCheck;
    super.visitScript(scriptTree);
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    if (check.isExtendsAndImplementsLine && tree.is(Kind.CLASS_DECLARATION)) {
      checkExtendsAndImplementsLine(tree);
    }
    super.visitClassDeclaration(tree);
  }

  private void checkExtendsAndImplementsLine(ClassDeclarationTree tree) {
    SyntaxToken classNameToken = tree.name().token();
    int nameLine = classNameToken.line();

    boolean isExtendsOnClassNameLine = isExtendsOnClassNameLine(tree, nameLine);
    boolean isImplementsOnClassNameLine = isImplementsOnClassNameLine(tree, nameLine);

    String partialMessage = getIssuePartialMessage(isExtendsOnClassNameLine, isImplementsOnClassNameLine);

    if (partialMessage != null) {
      check.reportIssue(String.format(MESSAGE, partialMessage, classNameToken.text()), tree.name());
    }
  }

  private static boolean isExtendsOnClassNameLine(ClassDeclarationTree classDeclaration, int classNameLine) {
    SyntaxToken extendsToken = classDeclaration.extendsToken();
    return extendsToken == null || classNameLine == extendsToken.line();
  }

  private static boolean isImplementsOnClassNameLine(ClassDeclarationTree classDeclaration, int classNameLine) {
    SyntaxToken implementsToken = classDeclaration.implementsToken();
    return implementsToken == null || classNameLine == implementsToken.line();
  }

  /**
   * Returns a string to complete the issue message depending on if "implements" and/or "extends" are
   * on the same line than the class name.
   * Return null if there is not issue to report.
   */
  @Nullable
  private static String getIssuePartialMessage(boolean isExtendsOnClassNameLine, boolean isImplementsOnClassNameLine) {
    String msg = null;

    if (!isExtendsOnClassNameLine && !isImplementsOnClassNameLine) {
      msg = "\"extends\" and \"implements\" keywords";
    }

    if (!isExtendsOnClassNameLine && isImplementsOnClassNameLine) {
      msg = "\"extends\" keyword";
    }

    if (isExtendsOnClassNameLine && !isImplementsOnClassNameLine) {
      msg = "\"implements\" keyword";
    }

    return msg;
  }

}
