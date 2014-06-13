/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Grammar;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;

import javax.annotation.Nullable;

@Rule(
  key = "S1782",
  priority = Priority.MINOR)
public class ExtendsImplementsAndClassNameOnSameLineCheck extends SquidCheck<Grammar> {

  @Override
  public void init() {
    subscribeTo(PHPGrammar.CLASS_DECLARATION);
  }

  @Override
  public void leaveNode(AstNode node) {
    AstNode identifier = node.getFirstChild(GenericTokenType.IDENTIFIER);
    String className = identifier.getTokenOriginalValue();
    int classNameLine = identifier.getTokenLine();


    boolean isExtendsOnClassNameLine = isExtendsOnClassNameLine(node, classNameLine);
    boolean isImplementsOnClassNameLine = isImplementsOnClassNameLine(node, classNameLine);

    String msg = getIssueMessage(isExtendsOnClassNameLine, isImplementsOnClassNameLine);

    if (msg != null) {
      getContext().createLineViolation(this, "Move {0} on line {1} where declaration of class name \"{2}\" begin.", node, msg, classNameLine, className);
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
