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
import org.apache.commons.lang.StringUtils;
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

import java.util.regex.Pattern;

@Rule(
  key = "S115",
  name = "Constant names should comply with a naming convention",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class ConstantNameCheck extends SquidCheck<LexerlessGrammar> {

  public static final String DEFAULT = "^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$";
  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    defaultValue = DEFAULT)
  String format = DEFAULT;


  @Override
  public void init() {
    pattern = Pattern.compile(format);
    subscribeTo(
      PHPGrammar.CLASS_CONSTANT_DECLARATION,
      PHPGrammar.FUNCTION_CALL_PARAMETER_LIST,
      PHPGrammar.CONSTANT_DECLARATION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (isCallToDefine(astNode)) {
      checkConstantName(astNode, getFirstParameter(astNode));
    } else if (astNode.is(PHPGrammar.CLASS_CONSTANT_DECLARATION, PHPGrammar.CONSTANT_DECLARATION)) {
      for (AstNode constDec : astNode.getChildren(PHPGrammar.MEMBER_CONST_DECLARATION, PHPGrammar.CONSTANT_VAR)) {
        checkConstantName(constDec, constDec.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue());
      }
    }
  }

  private void checkConstantName(AstNode node, String constName) {
    if (!pattern.matcher(constName).matches()) {
      getContext().createLineViolation(this, "Rename this constant \"{0}\" to match the regular expression {1}.", node, constName, format);
    }
  }

  private String getFirstParameter(AstNode astNode) {
    String firstParam = astNode.getFirstChild(PHPGrammar.PARAMETER_LIST_FOR_CALL).getFirstChild().getTokenOriginalValue();
    return StringUtils.substring(firstParam, 1, firstParam.length() - 1);
  }

  private static boolean isCallToDefine(AstNode node) {
    return node.is(PHPGrammar.FUNCTION_CALL_PARAMETER_LIST)
      && "define".equals(node.getPreviousAstNode().getTokenOriginalValue());
  }


}
