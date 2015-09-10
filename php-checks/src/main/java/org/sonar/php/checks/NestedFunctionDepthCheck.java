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
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;

@Rule(
  key = "S2004",
  name = "Functions should not be nested too deeply",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_CHANGEABILITY)
@SqaleConstantRemediation("20min")
public class NestedFunctionDepthCheck extends SquidCheck<LexerlessGrammar> {

  private int nestedLevel = 0;
  public static final int DEFAULT = 3;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  int max = DEFAULT;

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.METHOD_DECLARATION,
      PHPGrammar.FUNCTION_DECLARATION);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    nestedLevel = 0;
  }

  @Override
  public void visitNode(AstNode astNode) {
    nestedLevel++;

    if (nestedLevel == max + 1) {
      getContext().createLineViolation(this, "Refactor this code to not nest functions more than {0} levels deep.", astNode, max);
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    nestedLevel--;
  }
}
