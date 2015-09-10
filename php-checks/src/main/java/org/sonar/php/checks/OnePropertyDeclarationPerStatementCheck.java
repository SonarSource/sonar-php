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
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S1766",
  name = "More than one property should not be declared per statement",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION, Tags.PSR2})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class OnePropertyDeclarationPerStatementCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.CLASS_VARIABLE_DECLARATION,
      PHPGrammar.CLASS_CONSTANT_DECLARATION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    int nbDeclaration = astNode.getChildren(PHPGrammar.VARIABLE_DECLARATION, PHPGrammar.MEMBER_CONST_DECLARATION).size();

    if (nbDeclaration > 1) {
      getContext().createLineViolation(this, "{0} property declarations were found in this statement. Reformat the code to declare only one property per statement.",
        astNode,
        nbDeclaration);
    }
  }
}
