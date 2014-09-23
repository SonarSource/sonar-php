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

import com.google.common.collect.Maps;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Token;
import org.apache.commons.lang.StringUtils;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPTokenType;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.Map;

@Rule(
  key = "S1068",
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
public class UnusedPrivateFieldCheck extends SquidCheck<LexerlessGrammar> {

  private Map<String, PrivateField> privateFields = Maps.newHashMap();

  private static class PrivateField {
    final AstNode declaration;
    int usage = 0;

    private PrivateField(AstNode declaration) {
      this.declaration = declaration;
    }
  }

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.CLASS_DECLARATION,
      PHPGrammar.MEMBER_EXPRESSION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.CLASS_DECLARATION)) {
      retrievePrivateClassField(astNode);
    } else {
      check(astNode);
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.CLASS_DECLARATION)) {
      reportUnusedPrivateField();
      privateFields.clear();
    }
  }

  private void retrievePrivateClassField(AstNode classDeclaration) {
    for (AstNode classStmt : classDeclaration.getChildren(PHPGrammar.CLASS_STATEMENT)) {
      AstNode stmtChild = classStmt.getFirstChild();

      if (stmtChild.is(PHPGrammar.CLASS_VARIABLE_DECLARATION) && isPrivate(stmtChild)) {
        for (AstNode varDeclaration : stmtChild.getChildren(PHPGrammar.VARIABLE_DECLARATION)) {
          AstNode varIdentifier = varDeclaration.getFirstChild(PHPGrammar.VAR_IDENTIFIER);
          privateFields.put(getCalledName(varIdentifier, stmtChild.getFirstChild(PHPGrammar.VARIABLE_MODIFIERS)),
            new PrivateField(varIdentifier));
        }
      }
    }
  }


  /**
   * Returns "::$field" for static field and "$this->field" for others.
   */
  private String getCalledName(AstNode varIdentifier, AstNode varModifiers) {
    if (isStatic(varModifiers)) {
      return "::" + varIdentifier.getTokenOriginalValue();
    } else {
      return "$this->" + StringUtils.remove(varIdentifier.getTokenOriginalValue(), "$");
    }
  }

  private boolean isStatic(AstNode varModifiers) {
    for (AstNode modifier : varModifiers.getChildren(PHPGrammar.MEMBER_MODIFIER)) {
      if (modifier.getFirstChild().is(PHPKeyword.STATIC)) {
        return true;
      }
    }
    return false;
  }

  private boolean isPrivate(AstNode classVarDeclaration) {
    for (AstNode modifier : classVarDeclaration.getFirstChild(PHPGrammar.VARIABLE_MODIFIERS).getChildren(PHPGrammar.MEMBER_MODIFIER)) {
      if (modifier.getFirstChild().is(PHPKeyword.PRIVATE)) {
        return true;
      }
    }
    return false;
  }

  private void check(AstNode variable) {
    String varName = getVariableName(variable);
    PrivateField field = privateFields.get(varName);

    if (field != null) {
      field.usage++;
    }
  }

  private void reportUnusedPrivateField() {
    for (PrivateField field : privateFields.values()) {
      if (field.usage == 0) {
        getContext().createLineViolation(this, "Remove this unused \"{0}\" private field.", field.declaration, field.declaration.getTokenOriginalValue());
      }
    }
  }

  /**
   * Return variable full name excluding array access and keyword "self" and "static".
   * <p/>
   * Example:
   * <ol>
   * <li>for "$this->myArray[0]", function returns "$this->myArray"
   * <li>for "static::$field", function returns "::$field"
   */
  private String getVariableName(AstNode expr) {
    boolean exclude = false;
    StringBuilder builder = new StringBuilder();
    for (Token token : expr.getTokens()) {

      if ("static".equals(token.getOriginalValue()) || "self".equals(token.getOriginalValue())) {
        continue;
      }
      if ("[".equals(token.getOriginalValue())) {
        exclude = true;

      } else if ("]".equals(token.getOriginalValue())) {
        exclude = false;

      } else if (!exclude) {
        builder.append(token.getOriginalValue());
      }
    }

    return builder.toString();
  }
}
