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
import org.apache.commons.lang.StringUtils;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Rule(
  key = "S117",
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
public class LocalVariableAndParameterNameCheck extends SquidCheck<LexerlessGrammar> {

  public static final String DEFAULT = "^[a-z][a-zA-Z0-9]*$";
  private Deque<Set<String>> checkedVariables = new ArrayDeque<Set<String>>();
  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    defaultValue = DEFAULT)
  String format = DEFAULT;


  @Override
  public void init() {
    pattern = Pattern.compile(format);
    subscribeTo(CheckUtils.functions());
    subscribeTo(PHPGrammar.ASSIGNMENT_EXPR);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(CheckUtils.functions())) {
      enterScope();
      checkParameters(astNode);
    } else if (inScope()) {
      checkLocalVariable(astNode);
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(CheckUtils.functions())) {
      leaveScope();
    }
  }

  private void checkLocalVariable(AstNode assignmentExpr) {
    AstNode varNode = getLeftHandExpression(assignmentExpr);

    if (varNode != null && varNode.is(PHPGrammar.VARIABLE_WITHOUT_OBJECTS)) {
      String varName = varNode.getTokenOriginalValue();

      if (!isAlreadyChecked(varName) && !isCompliant(varName)) {
        reportIssue("local variable", varNode, varName);
      }
    }
  }

  private void checkParameters(AstNode functionDec) {
    for (AstNode parameter : CheckUtils.getFunctionParameters(functionDec)) {
      String paramName = parameter.getTokenOriginalValue();

      if (!isCompliant(paramName)) {
        reportIssue("parameter", parameter, paramName);
      }
    }
  }

  private void reportIssue(String msg, AstNode node, String varName) {
    getContext().createLineViolation(this, "Rename this {0} \"{1}\" to match the regular expression {2}.",
      node, msg, varName, format);
    setAsCheckedVariable(varName);
  }

  private AstNode getLeftHandExpression(AstNode assignmentExpr) {
    AstNode leftExpr = assignmentExpr.getFirstChild();
    AstNode variableNode = null;

    if (leftExpr.is(PHPGrammar.MEMBER_EXPRESSION)) {
      variableNode = leftExpr;
    } else if (leftExpr.is(PHPGrammar.POSTFIX_EXPR)) {
      variableNode = leftExpr.getFirstChild();
    }

    if (variableNode != null && variableNode.getNumberOfChildren() == 1) {
      return variableNode.getFirstChild();
    }

    return null;
  }

  private boolean isCompliant(String varName) {
    return pattern.matcher(StringUtils.remove(varName, "$")).matches();
  }

  private void setAsCheckedVariable(String varName) {
    checkedVariables.peek().add(varName);
  }

  private boolean isAlreadyChecked(String varName) {
    return checkedVariables.peek().contains(varName);
  }

  private boolean inScope() {
    return !checkedVariables.isEmpty();
  }

  private void enterScope() {
    checkedVariables.push(new HashSet<String>());
  }

  private void leaveScope() {
    checkedVariables.pop();
  }

}
