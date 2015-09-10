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
import org.sonar.check.RuleProperty;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S2042",
  name = "Classes should not have too many lines",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
@SqaleConstantRemediation("1h")
public class TooManyLinesInClassCheck extends SquidCheck<LexerlessGrammar> {

  private static final int DEFAULT = 200;

  @RuleProperty(
    key = "maximumLinesThreshold",
    defaultValue = "" + DEFAULT)
  public int maximumLinesThreshold = DEFAULT;

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.CLASS_DECLARATION,
      PHPGrammar.INTERFACE_DECLARATION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    int nbLines = getNumberOfLine(astNode);

    if (nbLines > maximumLinesThreshold) {
      getContext().createLineViolation(this, "Class \"{0}\" has {1} lines, which is greater than the {2} authorized. Split it into smaller classes.",
        astNode, astNode.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue(), nbLines, maximumLinesThreshold);
    }
  }

  public static int getNumberOfLine(AstNode classDeclaration) {
    int firstLine = classDeclaration.getFirstChild(PHPPunctuator.LCURLYBRACE).getTokenLine();
    int lastLine = classDeclaration.getFirstChild(PHPPunctuator.RCURLYBRACE).getTokenLine();

    return lastLine - firstLine + 1;
  }

}
