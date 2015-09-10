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
package org.sonar.php.checks.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Token;
import org.apache.commons.lang.StringUtils;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.List;
import java.util.Map;

public abstract class AbstractUnusedPrivateClassMemberCheck extends SquidCheck<LexerlessGrammar> {

  private Map<String, PrivateMember> privateMembers = Maps.newHashMap();

  private static class PrivateMember {
    final AstNode declaration;
    int usage = 0;

    private PrivateMember(AstNode declaration) {
      this.declaration = declaration;
    }
  }

  protected void addPrivateMember(String calledName, AstNode declaration) {
    privateMembers.put(calledName, new PrivateMember(declaration));
  }

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.CLASS_DECLARATION,
      PHPGrammar.MEMBER_EXPRESSION,
      PHPGrammar.SIMPLE_ENCAPS_VARIABLE,
      PHPGrammar.STRING_LITERAL);
  }


  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.CLASS_DECLARATION)) {
      retrievePrivateClassMember(astNode);

    } else if (astNode.is(PHPGrammar.STRING_LITERAL)) {
      checkUsageInString(astNode);

    } else {
      checkUsage(astNode);
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.CLASS_DECLARATION)) {
      reportUnusedPrivateField();
      privateMembers.clear();
    }
  }

  protected abstract void retrievePrivateClassMember(AstNode classDec);

  protected abstract String getIssueMessage();

  /**
   * Increase usage of a class member if its name appears in a string that does
   * not contains encapsulated variable.
   */
  protected void checkUsageInString(AstNode stringLiteral) {
    // String literal without encapsulated variable
    if (stringLiteral.getFirstChild().isNot(PHPGrammar.ENCAPS_STRING_LITERAL)) {

      for (PrivateMember member : privateMembers.values()) {
        if (stringLiteral.getTokenOriginalValue().contains(member.declaration.getTokenOriginalValue())) {
          member.usage++;
        }
      }
    }
  }

  /**
   * Create an issue when usage of a private member have not been found
   * in a file.
   */
  protected void reportUnusedPrivateField() {
    for (PrivateMember field : privateMembers.values()) {
      if (field.usage == 0) {
        getContext().createLineViolation(this, getIssueMessage(), field.declaration, field.declaration.getTokenOriginalValue());
      }
    }
  }

  /**
   * Check is the expression uses one of declared private member.
   */
  protected void checkUsage(AstNode variable) {
    for (String varName : getCalledMemberInExpression(variable)) {
      PrivateMember field = privateMembers.get(varName);

      if (field != null) {
        field.usage++;
      }
    }
  }

  /**
   * Returns "::$field" for static field and "->field" for others.
   *
   * @param identifierNode node that correspond to the name of the class member.
   */
  protected String getCalledName(AstNode identifierNode, List<AstNode> modifiers) {
    if (CheckUtils.isStaticClassMember(modifiers)) {
      return "::" + identifierNode.getTokenOriginalValue();
    } else {
      return "->" + StringUtils.remove(identifierNode.getTokenOriginalValue(), "$");
    }
  }

  protected boolean isPrivate(List<AstNode> memberModifiers) {
    for (AstNode modifier : memberModifiers) {
      if (modifier.getFirstChild().is(PHPKeyword.PRIVATE)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return a list called class member in the given expression, adding
   * empty parenthesis when its a method call.
   * <p/>
   * Example:
   * <ol>
   * <li>for "$this->myArray[0]", function returns "[->myArray]"
   * <li>for "$this->func(param)", function returns "[->func()]"
   * <li>for "static::$field", function returns "[::$field]"
   * <li>for "$this->setA()->setB()", function returns "[->setA(), setB()]"
   */
  protected List<String> getCalledMemberInExpression(AstNode var) {
    StringBuilder builder = new StringBuilder();
    List<String> list = Lists.newLinkedList();
    boolean consume = false;

    for (Token token : var.getTokens()) {
      String tokenValue = token.getOriginalValue();

      if (PHPPunctuator.ARROW.getValue().equals(tokenValue) || PHPPunctuator.DOUBLECOLON.getValue().equals(tokenValue)) {
        consume = true;

      } else if ("(".equals(tokenValue)) {
        builder.append("()");
        consume = false;

      } else if ("[".equals(tokenValue)) {
        consume = false;
      }

      if (consume) {
        builder.append(tokenValue);
      } else if (builder.length() > 0) {
        list.add(builder.toString());
        builder = new StringBuilder();
      }
    }

    if (builder.length() > 0) {
      list.add(builder.toString());
    }
    return list;
  }
}
