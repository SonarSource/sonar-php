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
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;

@Rule(
  key = "S2037",
  name = "Static members should be referenced with \"static::\"",
  priority = Priority.MAJOR,
  tags = {Tags.PITFALL})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.INSTRUCTION_RELIABILITY)
@SqaleConstantRemediation("2min")
public class SelfKeywordUsageCheck extends SquidCheck<LexerlessGrammar> {

  private boolean inFieldDeclaration = false;
  private static final GrammarRuleKey[] FIELDS_DECLARATION = {
    PHPGrammar.CLASS_VARIABLE_DECLARATION,
    PHPGrammar.CLASS_CONSTANT_DECLARATION
  };

  @Override
  public void init() {
    subscribeTo(PHPGrammar.CLASS_MEMBER_ACCESS);
    subscribeTo(FIELDS_DECLARATION);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    inFieldDeclaration = false;
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(FIELDS_DECLARATION)) {
      inFieldDeclaration = true;
    }

    AstNode caller = astNode.getPreviousAstNode();

    if (!inFieldDeclaration && "self".equals(caller.getTokenOriginalValue())) {
      getContext().createLineViolation(this, "Use \"static\" keyword instead of \"self\".", caller);
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(FIELDS_DECLARATION)) {
      inFieldDeclaration = false;
    }
  }

}
