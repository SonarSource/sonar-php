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
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S1656",
  name = "Variables should not be self-assigned",
  tags = {"bug", "cert"},
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.DATA_RELIABILITY)
@SqaleConstantRemediation("3min")
public class SelfAssignmentCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(PHPGrammar.ASSIGNMENT_EXPR, PHPGrammar.ASSIGNMENT_BY_REFERENCE);
  }

  @Override
  public void visitNode(AstNode node) {
    AstNode assignedValue = node.getLastChild();
    if (node.is(PHPGrammar.ASSIGNMENT_EXPR)) {
      if (isCompoundAssignment(node)) {
        return;
      }
      if (assignedValue.getFirstChild().getNumberOfChildren() == 1) {
        assignedValue = assignedValue.getFirstChild().getFirstChild();
      }
    }
    if (CheckUtils.areSyntacticallyEquivalent(node.getFirstChild(), assignedValue)) {
      getContext().createLineViolation(this, "Remove or correct this useless self-assignment", node);
    }
  }

  private boolean isCompoundAssignment(AstNode node) {
    return !node.getFirstChild(PHPGrammar.ASSIGNMENT_OPERATOR).getFirstChild().is(PHPPunctuator.EQU);
  }

}
