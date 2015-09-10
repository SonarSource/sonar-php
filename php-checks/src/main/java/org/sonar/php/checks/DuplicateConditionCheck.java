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
import com.sonar.sslr.api.AstNodeType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.AbstractDuplicateBranchCheck;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = "S1862",
  name = "Related \"if/else if\" statements and \"cases\" in a \"switch\" should not have the same condition",
  tags = {Tags.BUG, Tags.CERT, Tags.PITFALL, Tags.UNUSED},
  priority = Priority.CRITICAL)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.CRITICAL)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("10min")
public class DuplicateConditionCheck extends AbstractDuplicateBranchCheck {

  @Override
  protected AstNodeType ifBranchNodeType() {
    return PHPGrammar.PARENTHESIS_EXPRESSION;
  }

  @Override
  protected AstNodeType caseClauseChildType() {
    return PHPGrammar.EXPRESSION;
  }

  @Override
  protected void addIssue(String type, AstNode duplicate, AstNode duplicated) {
    String message = "This {0} duplicates the one on line {1}.";
    getContext().createLineViolation(this, message, duplicate, type, duplicated.getTokenLine());
  }
}
