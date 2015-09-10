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
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.formatting.ControlStructureSpacingCheck;
import org.sonar.php.checks.formatting.CurlyBraceCheck;
import org.sonar.php.checks.formatting.ExtendsImplementsLineCheck;
import org.sonar.php.checks.formatting.FunctionSpacingCheck;
import org.sonar.php.checks.formatting.IndentationCheck;
import org.sonar.php.checks.formatting.NamespaceAndUseStatementCheck;
import org.sonar.php.checks.formatting.PunctuatorSpacingCheck;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;

import java.util.Arrays;

@Rule(
  key = "S1808",
  name = "Source code should comply with formatting standards",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION, Tags.PSR2})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("1min")
public class FormattingStandardCheck extends SquidCheck<LexerlessGrammar> {

  private static final GrammarRuleKey[] CLASS_AND_FUNCTION = {
    PHPGrammar.CLASS_DECLARATION,
    PHPGrammar.INTERFACE_DECLARATION,
    PHPGrammar.TRAIT_ADAPTATIONS,
    PHPGrammar.METHOD_DECLARATION,
    PHPGrammar.FUNCTION_DECLARATION
  };

  private static final GrammarRuleKey[] CONTROL_STRUCTURE = {
    PHPGrammar.IF_STATEMENT,
    PHPGrammar.ELSEIF_CLAUSE,
    PHPGrammar.ELSE_CLAUSE,
    PHPGrammar.DO_WHILE_STATEMENT,
    PHPGrammar.WHILE_STATEMENT,
    PHPGrammar.FOR_STATEMENT,
    PHPGrammar.FOREACH_STATEMENT,
    PHPGrammar.SWITCH_STATEMENT,
    PHPGrammar.TRY_STATEMENT,
    PHPGrammar.CATCH_STATEMENT,
    PHPGrammar.FINALLY_STATEMENT
  };

  private final NamespaceAndUseStatementCheck namespaceAndUseStatementCheck = new NamespaceAndUseStatementCheck();
  private final CurlyBraceCheck curlyBraceCheck = new CurlyBraceCheck();
  private final PunctuatorSpacingCheck punctuatorSpacingCheck = new PunctuatorSpacingCheck();
  private final FunctionSpacingCheck functionSpacingCheck = new FunctionSpacingCheck();
  private final ControlStructureSpacingCheck controlStructureSpacingCheck = new ControlStructureSpacingCheck();
  private final IndentationCheck indentationCheck = new IndentationCheck();
  private final ExtendsImplementsLineCheck extendsImplementsLineCheck = new ExtendsImplementsLineCheck();

  /**
   * Namespace and use statement
   */
  @RuleProperty(
    key = "namespace_blank_line",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean hasNamespaceBlankLine = true;

  @RuleProperty(
    key = "use_after_namespace",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isUseAfterNamespace = true;

  @RuleProperty(
    key = "use_blank_line",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean hasUseBlankLine = true;

  /**
   * Curly brace
   */
  @RuleProperty(
    key = "open_curly_brace_classes_functions",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isOpenCurlyBraceForClassAndFunction = true;

  @RuleProperty(
    key = "open_curly_brace_control_structures",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isOpenCurlyBraceForControlStructures = true;

  @RuleProperty(
    key = "closing_curly_brace",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isClosingCurlyNextToKeyword = true;

  /**
   * Spacing
   */
  @RuleProperty(
    key = "one_space_after",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isOneSpaceBetweenRParentAndLCurly = true;

  @RuleProperty(
    key = "one_space_before",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isOneSpaceBetweenKeywordAndNextToken = true;

  @RuleProperty(
    key = "one_space_for",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isOneSpaceAfterForLoopSemicolon = true;

  @RuleProperty(
    key = "space_comma",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isOneSpaceAfterComma = true;

  @RuleProperty(
    key = "no_space_method_name",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isNoSpaceAfterMethodName = true;

  @RuleProperty(
    key = "foreach_space",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isSpaceForeachStatement = true;

  @RuleProperty(
    key = "no_space",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isNoSpaceParenthesis = true;

  @RuleProperty(
    key = "closure_format",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isClosureSpacing = true;

  /**
   * Indentation
   */
  @RuleProperty(
    key = "function_declaration_arguments_indentation",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isMethodArgumentsIndentation = true;

  @RuleProperty(
    key = "function_calls_arguments_indentation",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isFunctionCallsArgumentsIndentation = true;

  @RuleProperty(
    key = "interfaces_indentation",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isInterfacesIndentation = true;

  /**
   * Extends and implements line
   */
  @RuleProperty(
    key = "extends_implements_line",
    defaultValue = "true",
    type = "BOOLEAN")
  public boolean isExtendsAndImplementsLine = true;

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.NAMESPACE_STATEMENT,
      PHPGrammar.PARAMETER_LIST,
      PHPGrammar.FUNCTION_CALL_PARAMETER_LIST,
      PHPGrammar.FUNCTION_EXPRESSION,
      PHPGrammar.USE_STATEMENT);
    subscribeTo(CLASS_AND_FUNCTION);
    subscribeTo(CONTROL_STRUCTURE);
    subscribeTo(PHPPunctuator.RPARENTHESIS);
  }

  @Override
  public void visitNode(AstNode astNode) {
    namespaceAndUseStatementCheck.visitNode(this, astNode);
    curlyBraceCheck.visitNode(this, astNode);
    punctuatorSpacingCheck.visitNode(this, astNode);
    functionSpacingCheck.visitNode(this, astNode);
    controlStructureSpacingCheck.visitNode(this, astNode);
    indentationCheck.visitNode(this, astNode);
    extendsImplementsLineCheck.visitNode(this, astNode);
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    namespaceAndUseStatementCheck.leaveFile();
  }

  public void reportIssue(String msg, AstNode node) {
    getContext().createLineViolation(this, msg, node);
  }

  public static GrammarRuleKey[] getClassAndFunctionNodes() {
    return Arrays.copyOf(CLASS_AND_FUNCTION, CLASS_AND_FUNCTION.length);
  }

  public static GrammarRuleKey[] getControlStructureNodes() {
    return Arrays.copyOf(CONTROL_STRUCTURE, CONTROL_STRUCTURE.length);
  }
}
