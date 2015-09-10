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
import org.sonar.php.api.PHPPunctuator;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S2002",
  name = "Errors should not be silenced",
  priority = Priority.MAJOR,
  tags = {Tags.PITFALL})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.EXCEPTION_HANDLING)
@SqaleConstantRemediation("30min")
public class SilencedErrorsCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(PHPPunctuator.AT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    getContext().createLineViolation(this, "Remove the '@' symbol from this function call to un-silence errors.", astNode);
  }

}
