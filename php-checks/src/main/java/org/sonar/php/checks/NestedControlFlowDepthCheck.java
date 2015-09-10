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
import org.sonar.check.RuleProperty;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;

@Rule(
  key = "S134",
  name = "Control flow statements \"if\", \"for\", \"while\", \"switch\" and \"try\" should not be nested too deeply",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_CHANGEABILITY)
@SqaleConstantRemediation("10min")
public class NestedControlFlowDepthCheck extends SquidCheck<LexerlessGrammar> {

  private int nestingLevel;

  public static final int DEFAULT = 4;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  public int max = DEFAULT;


  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.IF_STATEMENT,
      PHPGrammar.ALTERNATIVE_IF_STATEMENT,
      PHPGrammar.DO_WHILE_STATEMENT,
      PHPGrammar.WHILE_STATEMENT,
      PHPGrammar.FOR_STATEMENT,
      PHPGrammar.FOREACH_STATEMENT,
      PHPGrammar.SWITCH_STATEMENT,
      PHPGrammar.TRY_STATEMENT);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    nestingLevel = 0;
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!isElseIf(astNode)) {
      nestingLevel++;
      if (nestingLevel == max + 1) {
        getContext().createLineViolation(this, "Refactor this code to not nest more than {0} \"if\", \"for\", \"while\", \"switch\" and \"try\" statements.", astNode, max);
      }
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (!isElseIf(astNode)) {
      nestingLevel--;
    }
  }

  private boolean isElseIf(AstNode astNode) {
    AstNode parentPreviousSibling = astNode.getParent().getPreviousSibling();
    return parentPreviousSibling != null && parentPreviousSibling.is(PHPKeyword.ELSE);
  }
}
