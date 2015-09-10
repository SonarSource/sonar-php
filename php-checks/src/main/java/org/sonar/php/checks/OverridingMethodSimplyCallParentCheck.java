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

import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.parser.LexicalConstant;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.List;
import java.util.regex.Pattern;

@Rule(
  key = "S1185",
  name = "Overriding methods should do more than simply call the same method in the super class",
  priority = Priority.MINOR,
  tags = Tags.CLUMSY)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("5min")
public class OverridingMethodSimplyCallParentCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(PHPGrammar.CLASS_DECLARATION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.hasDirectChildren(PHPGrammar.EXTENDS_FROM)) {
      String parentClassName = getParentClassName(astNode);

      for (AstNode method : getMethods(astNode)) {
        AstNode methodBody = method.getFirstChild(PHPGrammar.METHOD_BODY).getFirstChild();
        String methodName = method.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue();

        // Non-abstract
        if (methodBody.is(PHPGrammar.BLOCK) && isOnlyCallingParentMethod(method, methodBody.getFirstChild(PHPGrammar.INNER_STATEMENT_LIST), parentClassName, methodName)) {
          getContext().createLineViolation(this, "Remove this method \"{0}\" to simply inherit it.", method, methodName);
        }
      }
    }
  }

  private boolean isOnlyCallingParentMethod(AstNode method, AstNode stmtList, String parentClassName, String methodName) {
    if (stmtList != null && stmtList.getNumberOfChildren() == 1) {
      String expression = getExpressionAsString(stmtList);

      if (expression != null) {
        String expectedParam = buildExpectedParamRegexp(getNumberOfParameter(method.getFirstChild(PHPGrammar.PARAMETER_LIST)));
        // (parent || parentClassName) :: functionName ( params )
        String expectedExpression = "(?:parent|" + parentClassName.replace("\\", "\\\\") + ")::" + methodName + "\\(" + expectedParam + "\\)";

        return Pattern.matches(expectedExpression, expression);
      }
    }
    return false;
  }

  private String buildExpectedParamRegexp(int nbParam) {
    String expectedParam;

    if (nbParam == 0) {
      expectedParam = "";
    } else if (nbParam == 1) {
      expectedParam = LexicalConstant.VAR_IDENTIFIER;
    } else {
      expectedParam = "(?:" + LexicalConstant.VAR_IDENTIFIER + ",){" + (nbParam - 1) + ",}" + LexicalConstant.VAR_IDENTIFIER;
    }
    return expectedParam;
  }

  private String getExpressionAsString(AstNode stmtList) {
    AstNode stmt = stmtList.getFirstChild().getFirstChild();

    if (stmt.is(PHPGrammar.RETURN_STATEMENT) && stmt.hasDirectChildren(PHPGrammar.EXPRESSION)) {
      return CheckUtils.getExpressionAsString(stmt.getFirstChild(PHPGrammar.EXPRESSION));

    } else if (stmt.is(PHPGrammar.EXPRESSION_STATEMENT)) {
      return CheckUtils.getExpressionAsString(stmt.getFirstChild(PHPGrammar.EXPRESSION));

    } else {
      return null;
    }
  }

  private int getNumberOfParameter(AstNode paramList) {
    return paramList != null ? paramList.getChildren(PHPGrammar.PARAMETER).size() : 0;
  }

  private List<AstNode> getMethods(AstNode classDec) {
    List<AstNode> methods = Lists.newArrayList();

    for (AstNode classStmt : classDec.getChildren(PHPGrammar.CLASS_STATEMENT)) {
      AstNode stmtKind = classStmt.getFirstChild();

      if (stmtKind.is(PHPGrammar.METHOD_DECLARATION)) {
        methods.add(stmtKind);
      }
    }
    return methods;
  }

  private String getParentClassName(AstNode classDec) {
    return CheckUtils.getExpressionAsString(classDec.getFirstChild(PHPGrammar.EXTENDS_FROM).getFirstChild(PHPGrammar.FULLY_QUALIFIED_CLASS_NAME));
  }

}
