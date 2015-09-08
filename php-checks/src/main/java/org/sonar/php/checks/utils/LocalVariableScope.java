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
package org.sonar.php.checks.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sonar.sslr.api.AstNode;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.parser.PHPGrammar;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Map;

public class LocalVariableScope {
  List<String> exclusions = Lists.newArrayList();
  Map<String, Variable> localVariables = Maps.newHashMap();

  public Map<String, Variable> getLocalVariables() {
    return localVariables;
  }

  public void increaseUsageFor(String varName) {
    localVariables.get(varName).increaseUsage();
  }

  public void declareExclusion(String varName) {
    exclusions.add(varName);
  }

  private void declareLocalVariable(String varName, AstNode declaration) {
    localVariables.put(varName, new Variable(declaration));
  }

  private void declareLocalVariable(String varName, AstNode declaration, int usage) {
    localVariables.put(varName, new Variable(declaration, usage));
  }

  /**
   * Declare use globals variable in function as exclusions.
   *
   * @param globalVarStmt is GLOBAL_STATEMENT
   */
  public void declareGlobals(AstNode globalVarStmt) {
    Preconditions.checkArgument(globalVarStmt.is(PHPGrammar.GLOBAL_STATEMENT));

    for (AstNode globalVar : globalVarStmt.getFirstChild(PHPGrammar.GLOBAL_VAR_LIST).getChildren(PHPGrammar.GLOBAL_VAR)) {
      AstNode var = globalVar.getFirstChild(PHPGrammar.COMPOUND_VARIABLE).getFirstChild();

      if (var.is(PHPGrammar.VAR_IDENTIFIER)) {
        declareExclusion(var.getTokenOriginalValue());
      }
    }
  }

  /**
   * Declares function parameters as exclusions.
   *
   * @param functionDec is METHOD_DECLARATION, FUNCTION_DECLARATION or FUNCTION_EXPRESSION
   */
  public void declareParameters(AstNode functionDec) {
    Preconditions.checkArgument(functionDec.is(FunctionUtils.functions()));

    AstNode paramList = functionDec.getFirstChild(PHPGrammar.PARAMETER_LIST);
    if (paramList != null) {
      for (AstNode parameter : paramList.getChildren(PHPGrammar.PARAMETER)) {
        declareExclusion(parameter.getFirstChild(PHPGrammar.VAR_IDENTIFIER).getTokenOriginalValue());
      }
    }
  }

  /**
   * Declares static local variable of the function.
   *
   * @param staticStmt is STATIC_STATEMENT
   */
  public void declareStaticVariables(AstNode staticStmt) {
    Preconditions.checkArgument(staticStmt.is(PHPGrammar.STATIC_STATEMENT));

    for (AstNode staticVar : staticStmt.getFirstChild(PHPGrammar.STATIC_VAR_LIST).getChildren(PHPGrammar.STATIC_VAR)) {
      AstNode varIdentifier = staticVar.getFirstChild(PHPGrammar.VAR_IDENTIFIER);
      declareLocalVariable(varIdentifier.getTokenOriginalValue(), varIdentifier, 1);
    }
  }

  /**
   * Increases usage of the variable passed as parameter if this variable has
   * been declare as a local variable.
   *
   * @param varNode is VARIABLE_WITHOUT_OBJECTS or VARIABLE_IDENTIFIER, if others nothing will be done.
   */
  public void useVariable(AstNode varNode) {
    String varName;

    // Retrieve var name
    if (varNode.is(PHPGrammar.VARIABLE_WITHOUT_OBJECTS)) {
      varName = getVariableName(varNode);
    } else if (varNode.is(PHPGrammar.VAR_IDENTIFIER)) {
      varName = varNode.getTokenOriginalValue();
    } else {
      return;
    }

    // Increase usage if variable declared
    if (localVariables.containsKey(varName)) {
      increaseUsageFor(varName);
    }
  }

  /**
   * Increases usage of the variable passed as parameter if this variable has
   * been declare as a local variable.
   *
   * @param varName is the variable name to use
   */
  public void useVariale(String varName) {
    if (localVariables.containsKey(varName)) {
      increaseUsageFor(varName);
    }
  }

  /**
   * Declares variable as local variable of the function.
   *
   * @param variableWithoutObject is VARIABLE_WITHOUT_OBJECTS
   */
  public void declareVariable(AstNode variableWithoutObject) {
    Preconditions.checkArgument(variableWithoutObject.is(PHPGrammar.VARIABLE_WITHOUT_OBJECTS));

    String varName = getVariableName(variableWithoutObject);
    if (!isExcludedVariable(varName) && !localVariables.keySet().contains(varName)) {
      declareLocalVariable(varName, variableWithoutObject);
    }
  }

  /**
   * Check if the variable name correspond to variable that is not local variable,
   * excluded variables are:
   * <ul>
   * <li>$this
   * <li>super globals and predefined super globals: $GLOBALS, $_POST, etc.
   */
  private boolean isExcludedVariable(String varName) {
    return "$this".equals(varName) || CheckUtils.isSuperGlobal(varName) || exclusions.contains(varName);
  }

  /**
   * Returns variable name from node VARIABLE_WITHOUT_OBJECTS.
   */
  public static String getVariableName(AstNode variableWithoutObject) {
    Preconditions.checkArgument(variableWithoutObject.is(PHPGrammar.VARIABLE_WITHOUT_OBJECTS));
    return variableWithoutObject
      .getFirstChild(PHPGrammar.REFERENCE_VARIABLE)
      .getFirstChild(PHPGrammar.COMPOUND_VARIABLE).getTokenOriginalValue();
  }

  /**
   * Declare lexical variables as local variables. Multiple cases are handled:
   * <ul>
   * <li>if variable is declare in outer scope: increase usage for outer scope
   * and declares variable for current.
   * <li>if is reference variable: declare variable in outer and current scope.
   * <li>if variable not reference and not in outer scope: declared as an exclusion.
   * <li>if outer scope is null, variable is assumed to come from outer scope and excluded
   *
   * @param lexicalVarList list of variables.
   * @param outerScope     outer scope, if null means that outer scope is not a function
   */
  public void declareLexicalVariable(AstNode lexicalVarList, @Nullable LocalVariableScope outerScope) {
    Preconditions.checkArgument(lexicalVarList.is(PHPGrammar.LEXICAL_VAR_LIST));

    for (AstNode lexicalVar : lexicalVarList.getChildren(PHPGrammar.LEXICAL_VAR)) {
      AstNode varIdentifier = lexicalVar.getFirstChild(PHPGrammar.VAR_IDENTIFIER);
      String varName = varIdentifier.getTokenOriginalValue();
      boolean isReference = lexicalVar.hasDirectChildren(PHPPunctuator.AMPERSAND);
      boolean isFromOuterScope = outerScope == null || outerScope.localVariables.containsKey(varName);

      if (isReference && !isFromOuterScope) {
        this.declareLocalVariable(varName, varIdentifier, 1);
        outerScope.declareLocalVariable(varName, varIdentifier, 1);
      }

      if (outerScope == null) {
        this.declareExclusion(varName);
      } else if (isFromOuterScope) {
        this.declareLocalVariable(varName, varIdentifier, 1);
        outerScope.increaseUsageFor(varName);
      }

      if (!isReference && !isFromOuterScope) {
        this.declareExclusion(varName);
      }
    }
  }

  /**
   * Declares variable from list assignment:
   * <pre> list($a, $b) = array (1, 2);
   *
   * @param listExpr is LIST_EXPR
   */
  public void declareListVariable(AstNode listExpr) {
    for (AstNode listElement : listExpr.getFirstChild(PHPGrammar.ASSIGNMENT_LIST).getChildren(PHPGrammar.ASSIGNMENT_LIST_ELEMENT)) {
      AstNode child = listElement.getFirstChild();

      if (child.is(PHPGrammar.MEMBER_EXPRESSION) && child.getFirstChild().is(PHPGrammar.VARIABLE_WITHOUT_OBJECTS)) {
        AstNode varWithoutObject = child.getFirstChild();
        String varName = getVariableName(varWithoutObject);
        declareLocalVariable(varName, varWithoutObject);
      }
    }
  }

  /**
   * Declares variable from for each statement:
   * <pre>
   * // declare $v
   * foreach ($arr as &$v) {
   * }
   * </pre>
   *
   * When both key and value variables are define, value is declare with usage of 1
   * because the key cannot be retrieved without defining the value, e.g
   * <pre>
   * // $key declare with usage = 0 - $value with usage = 1
   * foreach($arr as $key => $value) {
   * }
   * </pre>
   *
   * @param foreachExpr is FOREACH_EXPR
   */
  public void declareForeachVariable(AstNode foreachExpr) {
    Preconditions.checkArgument(foreachExpr.is(PHPGrammar.FOREACH_EXPR));

    declareForeachVariable(foreachExpr.getFirstChild(PHPGrammar.FOREACH_VARIABLE), 0);

    if (foreachExpr.hasDirectChildren(PHPPunctuator.DOUBLEARROW)) {
      declareForeachVariable(foreachExpr.getLastChild(PHPGrammar.FOREACH_VARIABLE), 1);
    }

  }

  private void declareForeachVariable(AstNode foreachVar, int usage) {
    Preconditions.checkArgument(foreachVar.is(PHPGrammar.FOREACH_VARIABLE));

    AstNode memberExpr = foreachVar.getFirstChild(PHPGrammar.MEMBER_EXPRESSION);

    if (memberExpr.getNumberOfChildren() == 1 && memberExpr.getFirstChild().is(PHPGrammar.VARIABLE_WITHOUT_OBJECTS)) {
      AstNode varWithoutObject = memberExpr.getFirstChild();
      declareLocalVariable(getVariableName(varWithoutObject), varWithoutObject, usage);
    }
  }
}

