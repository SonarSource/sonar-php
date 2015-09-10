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
import org.sonar.check.RuleProperty;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S107",
  name = "Functions should not have too many parameters",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNIT_TESTABILITY)
@SqaleConstantRemediation("20min")
public class TooManyFunctionParametersCheck extends SquidCheck<LexerlessGrammar> {

  public static final int DEFAULT = 7;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  int max = DEFAULT;

  @Override
  public void init() {
    subscribeTo(PHPGrammar.PARAMETER_LIST);
  }

  @Override
  public void visitNode(AstNode astNode) {
    int nbParam = astNode.getChildren(PHPGrammar.PARAMETER).size();

    if (nbParam > max) {
      getContext().createLineViolation(this, "This function has {0} parameters, which is greater than the {1} authorized.", astNode, nbParam, max);
    }
  }
}
