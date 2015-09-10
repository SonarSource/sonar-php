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
package org.sonar.php.checks.formatting;

import com.sonar.sslr.api.AstNode;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.checks.FormattingStandardCheck;
import org.sonar.php.parser.PHPGrammar;

import javax.annotation.Nullable;

public class ExtendsImplementsLineCheck {

  public void visitNode(FormattingStandardCheck formattingCheck, AstNode node) {
    if (formattingCheck.isExtendsAndImplementsLine && node.is(PHPGrammar.CLASS_DECLARATION)) {
      checkExtendsAndImplementsLine(formattingCheck, node);
    }
  }

  private void checkExtendsAndImplementsLine(FormattingStandardCheck formattingCheck, AstNode node) {
    AstNode identifier = node.getFirstChild(PHPGrammar.IDENTIFIER);
    String className = identifier.getTokenOriginalValue();
    int classNameLine = identifier.getTokenLine();

    boolean isExtendsOnClassNameLine = isExtendsOnClassNameLine(node, classNameLine);
    boolean isImplementsOnClassNameLine = isImplementsOnClassNameLine(node, classNameLine);

    String msg = getIssueMessage(isExtendsOnClassNameLine, isImplementsOnClassNameLine);

    if (msg != null) {
      formattingCheck.reportIssue("Move " + msg + " to the same line as the declaration of its class name, \"" + className + "\".", node);
    }
  }

  private boolean isExtendsOnClassNameLine(AstNode classDeclaration, int classNameLine) {
    AstNode extendsNode = classDeclaration.getFirstChild(PHPGrammar.EXTENDS_FROM);
    if (extendsNode != null) {
      return classNameLine == extendsNode.getFirstChild(PHPKeyword.EXTENDS).getTokenLine();
    }

    return true;
  }

  private boolean isImplementsOnClassNameLine(AstNode classDeclaration, int classNameLine) {
    AstNode implementsNode = classDeclaration.getFirstChild(PHPGrammar.IMPLEMENTS_LIST);

    if (implementsNode != null) {
      return classNameLine == implementsNode.getFirstChild(PHPKeyword.IMPLEMENTS).getTokenLine();
    }

    return true;
  }

  /**
   * Returns a string to complete the issue message depending on if "implements" and/or "extends" are
   * on the same line than the class name.
   * Return null if there is not issue to report.
   */
  @Nullable
  private String getIssueMessage(boolean isExtendsOnClassNameLine, boolean isImplementsOnClassNameLine) {
    String msg = null;

    if (!isExtendsOnClassNameLine && !isImplementsOnClassNameLine) {
      msg = "\"extends\" and \"implements\" keywords";
    } else if (!isExtendsOnClassNameLine && isImplementsOnClassNameLine) {
      msg = "\"extends\" keyword";
    } else if (isExtendsOnClassNameLine && !isImplementsOnClassNameLine) {
      msg = "\"implements\" keyword";
    }

    return msg;
  }
}
