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
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

@Rule(
  key = "S1605",
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
public class CallParentConstructorCheck extends SquidCheck<LexerlessGrammar> {

  private Deque<String> scope = new ArrayDeque<String>();
  private boolean inConstructor = false;

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    scope.clear();
    inConstructor = false;
  }

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.CLASS_DECLARATION,
      PHPGrammar.METHOD_DECLARATION,
      PHPGrammar.CLASS_MEMBER_ACCESS);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.CLASS_DECLARATION)) {
      String parent = getExtendsFrom(astNode);
      if (parent != null) {
        scope.push(parent);
      }
    } else if (!scope.isEmpty()) {
      if (astNode.is(PHPGrammar.METHOD_DECLARATION) && isNonDeprecatedConstructor(astNode)) {
        inConstructor = true;
      } else if (inConstructor && isAccessingParentMember(astNode)) {
        String parentMemberName = astNode.getLastChild().getTokenOriginalValue();

        if (getCurrentClassParent().equals(parentMemberName)) {
          getContext().createLineViolation(this, "Replace \"parent::{0}(...)\" by \"parent::__construct(...)\".", astNode, parentMemberName);
        }
      }
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.METHOD_DECLARATION) && inConstructor) {
      inConstructor = false;
    } else if (!scope.isEmpty() && astNode.is(PHPGrammar.CLASS_DECLARATION)) {
      scope.pop();
    }
  }

  private String getCurrentClassParent() {
    return scope.peek();
  }

  private static boolean isAccessingParentMember(AstNode node) {
    return node.is(PHPGrammar.CLASS_MEMBER_ACCESS) && "parent".equals(node.getPreviousAstNode().getTokenOriginalValue());
  }

  private boolean isNonDeprecatedConstructor(AstNode astNode) {
    return "__construct".equals(astNode.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue());
  }

  private String getExtendsFrom(AstNode classDec) {
    AstNode extendsFrom = classDec.getFirstChild(PHPGrammar.EXTENDS_FROM);
    return extendsFrom != null ? extendsFrom.getFirstChild(PHPGrammar.FULLY_QUALIFIED_CLASS_NAME).getTokenOriginalValue() : null;
  }

}
