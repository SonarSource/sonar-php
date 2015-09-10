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
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S2047",
  name = "The names of methods with boolean return values should start with \"is\" or \"has\"",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("15min")
public class MethodNameReturningBooleanCheck extends SquidCheck<LexerlessGrammar> {

  private static final String RETURN_TAG = "@return";

  @Override
  public void init() {
    subscribeTo(PHPGrammar.METHOD_DECLARATION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (isReturningBoolean(astNode) && !hasBooleanPrefixName(astNode)) {
      getContext().createLineViolation(this, "Rename this method to start with \"is\" or \"has\".", astNode);
    }
  }

  private boolean hasBooleanPrefixName(AstNode methodDec) {
    String methodName = methodDec.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue();
    return methodName.startsWith("has") || methodName.startsWith("is");
  }

  private boolean isReturningBoolean(AstNode methodDec) {
    Token functionToken = methodDec.getToken();

    for (Trivia comment : functionToken.getTrivia()) {
      for (String line : comment.getToken().getOriginalValue().split(LexicalConstant.LINE_TERMINATOR)) {

        if (StringUtils.containsIgnoreCase(line, RETURN_TAG)) {
          return returnsBoolean(line);
        }
      }
    }
    return false;
  }

  private boolean returnsBoolean(String line) {
    boolean isPreviousReturnTag = false;

    for (String word : line.split("\\s")) {
      String s = word.trim();

      if (RETURN_TAG.equals(s)) {
        isPreviousReturnTag = true;

      } else if (isPreviousReturnTag) {
        return "bool".equalsIgnoreCase(s) || "boolean".equalsIgnoreCase(s);
      }
    }
    return false;
  }

}
