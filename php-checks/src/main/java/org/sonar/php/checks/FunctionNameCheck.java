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

import com.google.common.collect.ImmutableList;
import com.sonar.sslr.api.AstNode;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.FunctionUtils;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.regex.Pattern;

@Rule(
  key = "S100",
  name = "Function names should comply with a naming convention",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("5min")
public class FunctionNameCheck extends SquidCheck<LexerlessGrammar> {

  private static final ImmutableList<String> MAGIC_METHODS = ImmutableList.of(
    "__construct", "__destruct", "__call", "__callStatic", "__callStatic", "__get",
    "__set", "__isset", "__unset", "__sleep", "__wakeup", "__toString", "__invoke",
    "__set_state", "__clone", "__clone", "__debugInfo");
  public static final String DEFAULT = "^[a-z][a-zA-Z0-9]*$";
  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    defaultValue = DEFAULT)
  String format = DEFAULT;


  @Override
  public void init() {
    pattern = Pattern.compile(format);
    subscribeTo(
      PHPGrammar.METHOD_DECLARATION,
      PHPGrammar.FUNCTION_DECLARATION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    String functionName = astNode.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue();

    if (!pattern.matcher(functionName).matches() && !isExcluded(astNode, functionName)) {
      getContext().createLineViolation(this, "Rename function \"{0}\" to match the regular expression {1}.", astNode, functionName, format);
    }
  }

  private static boolean isExcluded(AstNode funcDec, String functionName) {
    return MAGIC_METHODS.contains(functionName) || FunctionUtils.isOverriding(funcDec);
  }
}
