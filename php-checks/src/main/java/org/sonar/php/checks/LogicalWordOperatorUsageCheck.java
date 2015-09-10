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
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S2010",
  name = "\"&&\" and \"||\" should be used",
  priority = Priority.CRITICAL,
  tags = {Tags.PITFALL})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.CRITICAL)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("5min")
public class LogicalWordOperatorUsageCheck extends SquidCheck<LexerlessGrammar> {
  @Override
  public void init() {
    subscribeTo(PHPKeyword.AND, PHPKeyword.OR);
  }

  @Override
  public void visitNode(AstNode astNode) {
    String punctuator = astNode.is(PHPKeyword.AND) ? PHPPunctuator.ANDAND.getValue() : PHPPunctuator.OROR.getValue();
    getContext().createLineViolation(this, "Replace \"{0}\" with \"{1}\".", astNode, astNode.getTokenOriginalValue(), punctuator);
  }

}
