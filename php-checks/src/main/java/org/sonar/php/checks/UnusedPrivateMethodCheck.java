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
package org.sonar.php.checks;

import com.sonar.sslr.api.AstNode;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.AbstractUnusedPrivateClassMemberCheck;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = "S1144",
  name = "Unused private method should be removed",
  priority = Priority.MAJOR,
  tags = {Tags.UNUSED})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("5min")
public class UnusedPrivateMethodCheck extends AbstractUnusedPrivateClassMemberCheck {

  @Override
  protected String getIssueMessage() {
    return "Remove this unused private \"{0}\" method.";
  }

  @Override
  protected void retrievePrivateClassMember(AstNode classDec) {
    for (AstNode classStmt : classDec.getChildren(PHPGrammar.CLASS_STATEMENT)) {
      AstNode stmtChild = classStmt.getFirstChild();

      if (stmtChild.is(PHPGrammar.METHOD_DECLARATION)) {
        List<AstNode> modifiers = stmtChild.getChildren(PHPGrammar.MEMBER_MODIFIER);
        AstNode identifier = stmtChild.getFirstChild(PHPGrammar.IDENTIFIER);
        String methodName = identifier.getTokenOriginalValue();

        if (isPrivate(modifiers) && !isConstructor(methodName, classDec) && !isMagicMethod(methodName)) {
          // Parenthesis specifies that member is a method
          addPrivateMember(getCalledName(identifier, modifiers) + "()", identifier);
        }
      }
    }
  }

  private static boolean isConstructor(String methodName, AstNode classDec) {
    return "__construct".equals(methodName) || classDec.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue().equals(methodName);
  }

  private static boolean isMagicMethod(String methodName) {
    return methodName.startsWith("__");
  }
}
