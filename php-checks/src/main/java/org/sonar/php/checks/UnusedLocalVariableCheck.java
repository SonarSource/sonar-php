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
import org.sonar.php.checks.utils.FunctionUtils;
import org.sonar.php.checks.utils.LocalVariableScope;
import org.sonar.php.checks.utils.Variable;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

@Rule(
  key = "S1481",
  name = "Unused local variables should be removed",
  priority = Priority.MAJOR,
  tags = {Tags.UNUSED})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("5min")
public class UnusedLocalVariableCheck extends SquidCheck<LexerlessGrammar> {

  private Deque<LocalVariableScope> scopes = new ArrayDeque<LocalVariableScope>();

  @Override
  public void init() {
    subscribeTo(FunctionUtils.functions());
    subscribeTo(
      PHPGrammar.GLOBAL_STATEMENT,
      PHPGrammar.STATIC_STATEMENT,
      PHPGrammar.LEXICAL_VAR_LIST,
      PHPGrammar.VAR_IDENTIFIER,
      PHPGrammar.SIMPLE_ENCAPS_VARIABLE,
      PHPGrammar.SEMI_COMPLEX_ENCAPS_VARIABLE,

      PHPGrammar.ASSIGNMENT_EXPR,
      PHPGrammar.ASSIGNMENT_BY_REFERENCE,
      PHPGrammar.LIST_EXPR,
      PHPGrammar.FOREACH_EXPR);
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    scopes.clear();
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(FunctionUtils.functions())) {
      scopes.push(new LocalVariableScope());
      getCurrentScope().declareParameters(astNode);
    } else if (!scopes.isEmpty()) {

      if (astNode.is(PHPGrammar.GLOBAL_STATEMENT)) {
        getCurrentScope().declareGlobals(astNode);

      } else if (astNode.is(PHPGrammar.STATIC_STATEMENT)) {
        getCurrentScope().declareStaticVariables(astNode);

      } else if (astNode.is(PHPGrammar.LEXICAL_VAR_LIST)) {
        getCurrentScope().declareLexicalVariable(astNode, getOuterScope());

      } else if (astNode.is(PHPGrammar.FOREACH_EXPR)) {
        getCurrentScope().declareForeachVariable(astNode);

      } else if (astNode.is(PHPGrammar.VAR_IDENTIFIER) && !isDeclaration(astNode)) {
        getCurrentScope().useVariable(astNode);

      } else if (astNode.is(PHPGrammar.SEMI_COMPLEX_ENCAPS_VARIABLE)) {
        getCurrentScope().useVariale("$" + astNode.getFirstChild(PHPGrammar.EXPRESSION).getTokenOriginalValue());

      } else if (astNode.is(PHPGrammar.ASSIGNMENT_EXPR, PHPGrammar.ASSIGNMENT_BY_REFERENCE)) {
        declareNewLocalVariable(astNode);

      } else if (astNode.is(PHPGrammar.LIST_EXPR)) {
        getCurrentScope().declareListVariable(astNode);
      }
    }
  }

  private boolean isDeclaration(AstNode varIdentifier) {
    AstNode parent = varIdentifier.getParent();
    return parent.getParent().is(PHPGrammar.GLOBAL_VAR) || parent.is(PHPGrammar.STATIC_VAR, PHPGrammar.LEXICAL_VAR);
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(FunctionUtils.functions())) {
      reportUnusedVariable();
      scopes.pop();
    }
  }

  private void declareNewLocalVariable(AstNode astNode) {
    AstNode leftExpr = getLeftHandExpression(astNode);
    if (leftExpr != null && leftExpr.is(PHPGrammar.VARIABLE_WITHOUT_OBJECTS)) {
      getCurrentScope().declareVariable(leftExpr);
    }
  }

  private AstNode getLeftHandExpression(AstNode assignmentExpr) {
    AstNode leftExpr = assignmentExpr.getFirstChild();

    if (leftExpr.is(PHPGrammar.MEMBER_EXPRESSION)) {
      return leftExpr.getFirstChild();
    } else if (leftExpr.is(PHPGrammar.POSTFIX_EXPR)) {
      return leftExpr.getFirstChild().getFirstChild();
    } else {
      return null;
    }
  }

  private void reportUnusedVariable() {
    for (Variable localVar : getCurrentScope().getLocalVariables().values()) {

      if (localVar.getUsage() == 1) {
        getContext().createLineViolation(this, "Remove this unused \"{0}\" local variable.", localVar.getDeclaration(),
          localVar.getDeclaration().getTokenOriginalValue());
      }
    }
  }

  private LocalVariableScope getCurrentScope() {
    return scopes.peek();
  }

  @Nullable
  private LocalVariableScope getOuterScope() {
    Iterator<LocalVariableScope> it = scopes.iterator();
    it.next(); // current
    return it.hasNext() ? it.next() /*previous*/ : null;
  }
}
