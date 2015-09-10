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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

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

import com.google.common.collect.Maps;
import com.sonar.sslr.api.AstNode;

@Rule(
  key = "S1117",
  name = "Local variables should not have the same name as class fields",
  priority = Priority.MAJOR,
  tags = {Tags.PITFALL})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.DATA_RELIABILITY)
@SqaleConstantRemediation("5min")
public class LocalVariableShadowsClassFieldCheck extends SquidCheck<LexerlessGrammar> {

  private ClassState classState = new ClassState();

  private static class ClassState {

    private Map<String, AstNode> classFields = Maps.newHashMap();
    private Deque<Set<String>> checkedVariables = new ArrayDeque<Set<String>>();
    private String className;

    public void clear() {
      classFields.clear();
      checkedVariables.clear();
    }

    public void setClassName(AstNode classDeclaration) {
      className = classDeclaration.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue();
    }

    public boolean isInClass() {
      return !classFields.isEmpty();
    }

    public void declareField(AstNode varIdentifier) {
      classFields.put(varIdentifier.getTokenOriginalValue(), varIdentifier);
    }

    public boolean hasFieldNamed(String paramName) {
      return classFields.containsKey(paramName);
    }

    public int getLineOfFieldNamed(String name) {
      return classFields.get(name).getTokenLine();
    }

    public void setAsCheckedVariable(String varName) {
      checkedVariables.peek().add(varName);
    }

    public boolean hasAlreadyBeenChecked(String varName) {
      return checkedVariables.peek().contains(varName);
    }

    public void newFunctionScope() {
      checkedVariables.push(new HashSet<String>());
    }

    public void leaveFunctionScope() {
      checkedVariables.pop();
    }
  }

  private boolean skip = false;

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.CLASS_DECLARATION,
      PHPGrammar.METHOD_DECLARATION,
      PHPGrammar.FUNCTION_EXPRESSION,
      PHPGrammar.ASSIGNMENT_EXPR,
      PHPGrammar.ASSIGNMENT_BY_REFERENCE);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    classState.clear();
    skip = false;
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.CLASS_DECLARATION)) {
      declareClassField(astNode);
      classState.setClassName(astNode);

    } else if (classState.isInClass() && astNode.is(PHPGrammar.METHOD_DECLARATION)) {
      if (isExcluded(astNode)) {
        skip = true;
      } else {
        classState.newFunctionScope();
        checkParameters(astNode);
      }

    } else if (astNode.is(PHPGrammar.FUNCTION_EXPRESSION) && classState.isInClass() && !skip) {
      classState.newFunctionScope();
      checkParameters(astNode);

    } else if (classState.isInClass() && !skip) {
      checkLocalVariable(astNode);
    }
  }

  private boolean isExcluded(AstNode methodDec) {
    String methodName = methodDec.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue();
    return CheckUtils.isStaticClassMember(methodDec.getChildren(PHPGrammar.MEMBER_MODIFIER))
      || isConstructor(methodName) || isSetter(methodName);
  }

  private void checkLocalVariable(AstNode assignmentExpr) {
    AstNode varNode = assignmentExpr.getFirstChild();
    String varName = CheckUtils.getExpressionAsString(varNode);

    if (classState.hasFieldNamed(varName) && !classState.hasAlreadyBeenChecked(varName)) {
      reportIssue(varNode, varName);
    }
  }

  private void checkParameters(AstNode functionDec) {
    for (AstNode parameter : FunctionUtils.getFunctionParameters(functionDec)) {
      String name = parameter.getTokenOriginalValue();

      if (classState.hasFieldNamed(name)) {
        reportIssue(parameter, name);
      }
    }
  }

  private void reportIssue(AstNode node, String varName) {
    getContext().createLineViolation(this, "Rename \"{0}\" which has the same name as the field declared at line {1}.",
      node, varName, classState.getLineOfFieldNamed(varName));
    classState.setAsCheckedVariable(varName);
  }

  private boolean isSetter(String methodName) {
    return methodName.length() > 2 && "set".equalsIgnoreCase(methodName.substring(0, 3));
  }

  private boolean isConstructor(String methodName) {
    return classState.className.equals(methodName) || "__construct".equals(methodName);
  }

  private void declareClassField(AstNode classDeclaration) {
    for (AstNode classStatement : classDeclaration.getChildren(PHPGrammar.CLASS_STATEMENT)) {
      AstNode stmt = classStatement.getFirstChild();

      if (stmt.is(PHPGrammar.CLASS_VARIABLE_DECLARATION)) {
        for (AstNode varDeclaration : stmt.getChildren(PHPGrammar.VARIABLE_DECLARATION)) {
          classState.declareField(varDeclaration.getFirstChild(PHPGrammar.VAR_IDENTIFIER));
        }
      }
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.CLASS_DECLARATION)) {
      classState.clear();

    } else if (astNode.is(PHPGrammar.METHOD_DECLARATION, PHPGrammar.FUNCTION_EXPRESSION) && !skip && classState.isInClass()) {
      classState.leaveFunctionScope();

    } else if (astNode.is(PHPGrammar.METHOD_DECLARATION) && skip) {
      skip = false;
    }
  }
}
