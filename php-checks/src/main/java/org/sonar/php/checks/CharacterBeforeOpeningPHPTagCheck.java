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

import java.util.regex.Pattern;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.php.parser.PHPTokenType;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Token;

@Rule(
  key = "S2000",
  name = "Files should not contain characters before \"<?php\"",
  priority = Priority.CRITICAL,
  tags = {Tags.USER_EXPERIENCE})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.INSTRUCTION_RELIABILITY)
@SqaleConstantRemediation("2min")
public class CharacterBeforeOpeningPHPTagCheck extends SquidCheck<LexerlessGrammar> {

  private static final Pattern OPENING_TAG = Pattern.compile(LexicalConstant.PHP_OPENING_TAG);

  @Override
  public void init() {
    subscribeTo(PHPTokenType.FILE_OPENING_TAG);
  }

  @Override
  public void visitNode(AstNode astNode) {
    Token token = astNode.getToken();

    if (token.getColumn() != 0 || token.getLine() != 1 || !OPENING_TAG.matcher(token.getOriginalValue()).matches()) {
      getContext().createLineViolation(this, "Remove the extra characters before the open tag.", astNode);
    }
  }

}
