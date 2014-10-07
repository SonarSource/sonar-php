/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S138",
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
public class TooManyLinesInFunctionCheck extends SquidCheck<LexerlessGrammar> {

  private static final int DEFAULT = 100;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  public int max = DEFAULT;

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.METHOD_DECLARATION,
      PHPGrammar.FUNCTION_DECLARATION,
      PHPGrammar.FUNCTION_EXPRESSION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    int nbLines = getNumberOfLine(astNode);

    if (nbLines > max) {
      getContext().createLineViolation(this, "This function {0} has {1} lines, which is greater than the {2} lines authorized. Split it into smaller functions.",
        astNode, CheckUtils.getFunctionName(astNode), nbLines, max);
    }
  }

  public static int getNumberOfLine(AstNode functionNode) {
    if (CheckUtils.isAbstractMethod(functionNode)) {
      return 0;
    }
    AstNode functionBlock = getFunctionBlock(functionNode);

    int firstLine = functionBlock.getFirstChild(PHPPunctuator.LCURLYBRACE).getTokenLine();
    int lastLine = functionBlock.getFirstChild(PHPPunctuator.RCURLYBRACE).getTokenLine();

    return lastLine - firstLine + 1;
  }

  private static AstNode getFunctionBlock(AstNode functionNode) {
    if (functionNode.is(PHPGrammar.METHOD_DECLARATION)) {
      return functionNode.getFirstChild(PHPGrammar.METHOD_BODY).getFirstChild(PHPGrammar.BLOCK);
    } else {
      return functionNode.getFirstChild(PHPGrammar.BLOCK);
    }
  }
}
