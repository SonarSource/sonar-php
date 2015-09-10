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

import com.google.common.collect.ImmutableMap;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.Map;

@Rule(
  key = "S1764",
  name = "Identical expressions should not be used on both sides of a binary operator",
  priority = Priority.CRITICAL,
  tags = {Tags.BUG, Tags.CERT})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.CRITICAL)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("2min")
public class IdenticalOperandsInBinaryExpressionCheck extends SquidCheck<LexerlessGrammar> {

  private static final Map<AstNodeType, AstNodeType> OPERATOR_TYPE_BY_EXPRESSION_TYPE =
    ImmutableMap.<AstNodeType, AstNodeType>builder()
      .put(PHPGrammar.EQUALITY_EXPR, PHPGrammar.EQUALITY_OPERATOR)
      .put(PHPGrammar.LOGICAL_AND_EXPR, PHPGrammar.LOGICAL_AND_OPERATOR)
      .put(PHPGrammar.LOGICAL_OR_EXPR, PHPGrammar.LOGICAL_OR_OPERATOR)
      .put(PHPGrammar.MULTIPLICATIVE_EXPR, PHPGrammar.MULIPLICATIVE_OPERATOR)
      .put(PHPGrammar.ADDITIVE_EXPR, PHPGrammar.ADDITIVE_OPERATOR)
      .put(PHPGrammar.RELATIONAL_EXPR, PHPGrammar.RELATIONAL_OPERATOR)
      .put(PHPGrammar.SHIFT_EXPR, PHPGrammar.SHIFT_OPERATOR)
      .build();

  private static final AstNodeType[] EXCLUDED_OPERATOR_TYPES = {PHPPunctuator.STAR, PHPPunctuator.PLUS};

  @Override
  public void init() {
    for (AstNodeType expressionType : OPERATOR_TYPE_BY_EXPRESSION_TYPE.keySet()) {
      subscribeTo(expressionType);
    }
  }

  @Override
  public void visitNode(AstNode expression) {
    AstNodeType expressionType = expression.getType();
    AstNodeType operatorType = OPERATOR_TYPE_BY_EXPRESSION_TYPE.get(expressionType);
    AstNode operator = expression.getFirstChild(operatorType);
    if (!operator.getFirstChild().is(EXCLUDED_OPERATOR_TYPES) && hasIdenticalOperands(operator) && !isLeftShiftBy1(operator)) {
      String operatorValue = operator.getTokenOriginalValue();
      String message = "Identical sub-expressions on both sides of operator \"{0}\"";
      getContext().createLineViolation(this, message, expression, operatorValue);
    }
  }

  private boolean hasIdenticalOperands(AstNode operator) {
    return CheckUtils.areSyntacticallyEquivalent(operator.getPreviousSibling(), operator.getNextSibling());
  }

  private boolean isLeftShiftBy1(AstNode operator) {
    AstNode operand = operator.getPreviousSibling();
    return operator.getFirstChild().is(PHPPunctuator.SL) && operand.getNumberOfChildren() == 1 && "1".equals(operand.getTokenValue());
  }

}
