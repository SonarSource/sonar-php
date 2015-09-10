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

import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstNode;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUtils;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

@Rule(
  key = "S2007",
  name = "Functions and variables should not be defined outside of classes",
  priority = Priority.MAJOR,
  tags = {Tags.CONVENTION})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNIT_TESTABILITY)
@SqaleConstantRemediation("15min")
public class FunctionDefineOutsideClassCheck extends SquidCheck<LexerlessGrammar> {

  private final Deque<AstNode> scopes = new ArrayDeque<>();
  private final Set<String> globalVariableNames = Sets.newHashSet();

  @Override
  public void init() {
    subscribeTo(FunctionUtils.functions());
    subscribeTo(PHPGrammar.ASSIGNMENT_EXPR);
  }

  @Override
  public void leaveFile(AstNode astNode) {
    scopes.clear();
    globalVariableNames.clear();
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.FUNCTION_DECLARATION)) {
      getContext().createLineViolation(this, "Move this function into a class.", astNode);
    } else if (scopes.isEmpty()) {
      checkAssignment(astNode);
    }
    if (astNode.is(FunctionUtils.functions())) {
      scopes.push(astNode);
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(FunctionUtils.functions())) {
      scopes.pop();
    }
  }

  private void checkAssignment(AstNode astNode) {
    AstNode memberExpr = astNode.getFirstChild();
    AstNode variable = memberExpr.getFirstChild();
    if (memberExpr.getNumberOfChildren() == 1 && variable.is(PHPGrammar.VARIABLE_WITHOUT_OBJECTS)) {
      String varName = variable.getTokenOriginalValue();
      if (memberExpr.getTokens().size() == 1 && !CheckUtils.isSuperGlobal(varName) && !globalVariableNames.contains(varName)) {
        getContext().createLineViolation(this, "Move this variable into a class.", astNode);
        globalVariableNames.add(varName);
      }
    }
  }

}
