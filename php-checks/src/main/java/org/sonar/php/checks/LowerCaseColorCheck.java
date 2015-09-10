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

import java.util.regex.Pattern;

@Rule(
  key = "S2038",
  name = "Colors should be defined in upper case",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class LowerCaseColorCheck extends SquidCheck<LexerlessGrammar> {

  private static final Pattern COLOR_REGEXP = Pattern.compile("#[A-Fa-f0-9]{3,6}");
  private static final Pattern COLOR_REGEXP_UPPER_CASE = Pattern.compile("#[A-F0-9]{3,6}");


  @Override
  public void init() {
    subscribeTo(PHPGrammar.STRING_LITERAL);
  }

  @Override
  public void visitNode(AstNode astNode) {
    // Skip string with encapsulated variables
    if (astNode.getFirstChild().isNot(PHPGrammar.ENCAPS_STRING_LITERAL)) {
      String stringContent = getStringContent(astNode);

      if (isLowerCaseColor(stringContent)) {
        getContext().createLineViolation(this, "Replace \"{0}\" with \"{1}\".", astNode, stringContent, stringContent.toUpperCase());
      }
    }
  }

  private static boolean isLowerCaseColor(String str) {
    return COLOR_REGEXP.matcher(str).matches() && !COLOR_REGEXP_UPPER_CASE.matcher(str).matches();
  }

  private static String getStringContent(AstNode stringLiteral) {
    String stringContent = stringLiteral.getTokenOriginalValue();
    return stringContent.substring(1, stringContent.length() - 1);
  }

}
