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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.sonar.sslr.api.AstNode;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.FunctionUtils;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.Map;

@Rule(
  key = "S1172",
  name = "Unused function parameters should be removed",
  priority = Priority.MAJOR,
  tags = {Tags.UNUSED, Tags.MISRA})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("5min")
public class UnusedFunctionParametersCheck extends SquidCheck<LexerlessGrammar> {

  private static final GrammarRuleKey[] FUNCTION_DECLARATIONS = {
    PHPGrammar.METHOD_DECLARATION,
    PHPGrammar.FUNCTION_DECLARATION,
    PHPGrammar.FUNCTION_EXPRESSION};

  private static class Scope {
    private final Scope outerScope;
    private final AstNode functionDec;
    private final Map<String, Integer> arguments;

    public Scope(Scope outerScope, AstNode functionDec) {
      this.outerScope = outerScope;
      this.functionDec = functionDec;
      this.arguments = Maps.newLinkedHashMap();
    }

    private void declare(AstNode astNode) {
      Preconditions.checkState(astNode.is(PHPGrammar.VAR_IDENTIFIER));

      String identifier = astNode.getTokenValue();
      arguments.put(identifier, 0);
    }

    private void use(AstNode astNode) {
      use(astNode.getTokenValue());
    }

    private void use(String varName) {
      Scope scope = this;

      while (scope != null) {
        Integer usage = scope.arguments.get(varName);
        if (usage != null) {
          usage++;
          scope.arguments.put(varName, usage);
          return;
        }
        scope = scope.outerScope;
      }
    }

  }

  private Scope currentScope;

  @Override
  public void init() {
    subscribeTo(FUNCTION_DECLARATIONS);
    subscribeTo(
      PHPGrammar.PARAMETER_LIST,
      PHPGrammar.VAR_IDENTIFIER,
      PHPGrammar.SEMI_COMPLEX_ENCAPS_VARIABLE);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(FUNCTION_DECLARATIONS) && !FunctionUtils.isAbstractMethod(astNode)) {
      // enter new scope
      currentScope = new Scope(currentScope, astNode);

    } else if (currentScope != null) {

      if (astNode.is(PHPGrammar.PARAMETER_LIST)) {
        initParametersList(astNode);

      } else if (isVarIdentifierInsideFunction(astNode)) {
        currentScope.use(astNode);

      } else if (astNode.is(PHPGrammar.SEMI_COMPLEX_ENCAPS_VARIABLE)) {
        currentScope.use("$" + astNode.getFirstChild(PHPGrammar.EXPRESSION, PHPGrammar.SEMI_COMPLEX_RECOVERY_EXPRESSION).getTokenOriginalValue());
      }
    }
  }

  private boolean isVarIdentifierInsideFunction(AstNode node) {
    return node.getParent().isNot(PHPGrammar.PARAMETER) && node.is(PHPGrammar.VAR_IDENTIFIER);
  }

  private void initParametersList(AstNode parameterListNode) {
    for (AstNode parameterNode : parameterListNode.getChildren(PHPGrammar.PARAMETER)) {
      currentScope.declare(parameterNode.getFirstChild(PHPGrammar.VAR_IDENTIFIER));
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(FUNCTION_DECLARATIONS) && !FunctionUtils.isAbstractMethod(astNode)) {
      // leave scope
      if (!FunctionUtils.isOverriding(astNode) && !FunctionUtils.mayOverrideAnotherMethod(astNode)) {
        reportUnusedArguments();
      }
      currentScope = currentScope.outerScope;
    }
  }

  @Override
  public void leaveFile(AstNode astNode) {
    currentScope = null;
  }

  public void reportUnusedArguments() {
    int nbUnusedArgs = 0;
    StringBuilder builder = new StringBuilder();

    for (Map.Entry<String, Integer> entry : currentScope.arguments.entrySet()) {
      if (entry.getValue() == 0) {
        builder.append(entry.getKey() + " ");
        nbUnusedArgs++;
      }
    }

    createIssue(builder, nbUnusedArgs);
  }

  public void createIssue(StringBuilder builder, int nbArgs) {
    if (nbArgs > 1) {
      String argsList = StringUtils.join(builder.toString().split(" "), ", ");
      getContext().createLineViolation(this, "Remove the unused function parameters \"" + argsList + "\".", currentScope.functionDec);
    } else if (nbArgs == 1) {
      getContext().createLineViolation(this, "Remove the unused function parameter \"" + builder.toString().trim() + "\".", currentScope.functionDec);
    }
  }

}
