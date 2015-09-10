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
  key = "S1068",
  name = "Unused private fields should be removed",
  priority = Priority.MAJOR,
  tags = {Tags.UNUSED})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("5min")
public class UnusedPrivateFieldCheck extends AbstractUnusedPrivateClassMemberCheck {

  @Override
  protected String getIssueMessage() {
    return "Remove this unused \"{0}\" private field.";
  }

  @Override
  protected void retrievePrivateClassMember(AstNode classDeclaration) {
    for (AstNode classStmt : classDeclaration.getChildren(PHPGrammar.CLASS_STATEMENT)) {
      AstNode stmtChild = classStmt.getFirstChild();

      if (stmtChild.is(PHPGrammar.CLASS_VARIABLE_DECLARATION)) {
        List<AstNode> modifiers = stmtChild.getFirstChild(PHPGrammar.VARIABLE_MODIFIERS).getChildren(PHPGrammar.MEMBER_MODIFIER);

        if (isPrivate(modifiers)) {
          for (AstNode varDeclaration : stmtChild.getChildren(PHPGrammar.VARIABLE_DECLARATION)) {
            AstNode varIdentifier = varDeclaration.getFirstChild(PHPGrammar.VAR_IDENTIFIER);

            addPrivateMember(getCalledName(varIdentifier, modifiers), varIdentifier);
          }
        }
      }
    }
  }

}
