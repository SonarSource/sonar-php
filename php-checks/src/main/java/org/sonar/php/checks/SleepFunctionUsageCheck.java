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
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S2964",
  name = "\"sleep\" should not be called",
  priority = Priority.CRITICAL,
  tags = {Tags.SECURITY})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.SECURITY_FEATURES)
@SqaleConstantRemediation("15min")
public class SleepFunctionUsageCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(PHPGrammar.MEMBER_EXPRESSION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (isSleepFunctionCall(astNode)) {
      getContext().createLineViolation(this, "Remove this call to \"sleep\".", astNode);
    }
  }

  private static boolean isSleepFunctionCall(AstNode memberExpr) {
    return memberExpr.getNumberOfChildren() == 2
      && memberExpr.getLastChild().is(PHPGrammar.FUNCTION_CALL_PARAMETER_LIST)
      && "sleep".equals(CheckUtils.getExpressionAsString(memberExpr.getFirstChild()));
  }

}
