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
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S1990",
  name = "\"final\" should not be used redundantly",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class RedundantFinalCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(PHPGrammar.CLASS_DECLARATION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (isFinalClass(astNode)) {

      for (AstNode classStatement : astNode.getChildren(PHPGrammar.CLASS_STATEMENT)) {
        AstNode statement = classStatement.getFirstChild();

        if (statement.is(PHPGrammar.METHOD_DECLARATION) && hasFinalModifier(statement)) {
          getContext().createLineViolation(this, "Remove this \"final\" modifier.", statement);
        }
      }
    }
  }

  private boolean hasFinalModifier(AstNode methodDec) {
    for (AstNode modifier : methodDec.getChildren(PHPGrammar.MEMBER_MODIFIER)) {

      if (modifier.getFirstChild().is(PHPKeyword.FINAL)) {
        return true;
      }
    }
    return false;
  }

  private boolean isFinalClass(AstNode classDec) {
    AstNode classType = classDec.getFirstChild(PHPGrammar.CLASS_ENTRY_TYPE).getFirstChild(PHPGrammar.CLASS_TYPE);
    return classType != null && classType.getFirstChild().is(PHPKeyword.FINAL);
  }
}
