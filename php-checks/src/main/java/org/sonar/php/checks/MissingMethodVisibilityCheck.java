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

import com.google.common.collect.ImmutableSet;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.Set;

@Rule(
  key = "S1784",
  priority = Priority.MINOR)
public class MissingMethodVisibilityCheck extends SquidCheck<LexerlessGrammar> {

  private static final Set<PHPKeyword> VISIBILITIES = ImmutableSet.of(
    PHPKeyword.PRIVATE,
    PHPKeyword.PROTECTED,
    PHPKeyword.PUBLIC);

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.METHOD_DECLARATION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!hasVisibility(astNode)) {
      getContext().createLineViolation(this, "Explicitly mention the visibility of this {0}.", astNode, getMessageForMethodName(astNode));
    }
  }

  private boolean hasVisibility(AstNode astNode) {
    for (AstNode modifier : astNode.getChildren(PHPGrammar.MEMBER_MODIFIER)) {
      if (VISIBILITIES.contains(modifier.getFirstChild().getType())) {
        return true;
      }
    }
    return false;
  }

  private String getMessageForMethodName(AstNode methodNode) {
    StringBuilder builder = new StringBuilder();
    String name = methodNode.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue();

    if (isConstructor(methodNode, name)) {
      builder.append("constructor ");
    } else if ("__destruct".equals(name)) {
      builder.append("destructor ");
    } else {
      builder.append("method ");
    }

    builder.append("\"" + name + "\"");

    return builder.toString();
  }

  private boolean isConstructor(AstNode methodNode, String methodName) {
    AstNode grandParent = methodNode.getParent().getParent();
    boolean isConstructorBeforePHP5_3_3 = false;

    if (grandParent.is(PHPGrammar.CLASS_DECLARATION)) {
      isConstructorBeforePHP5_3_3 = methodName.equals(grandParent.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue());
    }

    return isConstructorBeforePHP5_3_3 || "__construct".equals(methodName);
  }

}
