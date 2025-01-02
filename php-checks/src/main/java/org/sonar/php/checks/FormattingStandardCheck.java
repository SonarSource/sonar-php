/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import java.util.Locale;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.checks.formatting.ControlStructureSpacingCheck;
import org.sonar.php.checks.formatting.CurlyBraceCheck;
import org.sonar.php.checks.formatting.ExtendsImplementsLineCheck;
import org.sonar.php.checks.formatting.FormattingCheck;
import org.sonar.php.checks.formatting.FunctionSpacingCheck;
import org.sonar.php.checks.formatting.IndentationCheck;
import org.sonar.php.checks.formatting.NamespaceAndUseStatementCheck;
import org.sonar.php.checks.formatting.PunctuatorSpacingCheck;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = "S1808")
public class FormattingStandardCheck extends PHPVisitorCheck {

  private static final FormattingCheck[] SUB_CHECKS = {
    new NamespaceAndUseStatementCheck(),
    new CurlyBraceCheck(),
    new PunctuatorSpacingCheck(),
    new FunctionSpacingCheck(),
    new ControlStructureSpacingCheck(),
    new IndentationCheck(),
    new ExtendsImplementsLineCheck()
  };

  /**
   * Namespace and use statement
   */
  @RuleProperty(
    key = "namespace_blank_line",
    defaultValue = "true",
    description = "Enables the validation of required blank lines after namespace declaration.",
    type = "BOOLEAN")
  public boolean hasNamespaceBlankLine = true;

  @RuleProperty(
    key = "use_after_namespace",
    defaultValue = "true",
    description = "Enables the validation of the order of namespace declaration and use statements",
    type = "BOOLEAN")
  public boolean isUseAfterNamespace = true;

  @RuleProperty(
    key = "use_blank_line",
    defaultValue = "true",
    description = "Enables the validation of required blank lines after use statements.",
    type = "BOOLEAN")
  public boolean hasUseBlankLine = true;

  /**
   * Curly brace
   */
  @RuleProperty(
    key = "open_curly_brace_classes_functions",
    defaultValue = "true",
    description = "Enables the validation of open curly braces on new lines for function and class declarations.",
    type = "BOOLEAN")
  public boolean isOpenCurlyBraceForClassAndFunction = true;

  @RuleProperty(
    key = "open_curly_brace_control_structures",
    defaultValue = "true",
    description = "Enables the validation of open curly braces on new lines for control structures.",
    type = "BOOLEAN")
  public boolean isOpenCurlyBraceForControlStructures = true;

  @RuleProperty(
    key = "closing_curly_brace",
    defaultValue = "true",
    description = "Enables the verification that closing curly braces which follow keywords are on the same line as the keyword.",
    type = "BOOLEAN")
  public boolean isClosingCurlyNextToKeyword = true;

  /**
   * Spacing
   */
  @RuleProperty(
    key = "one_space_after",
    defaultValue = "true",
    description = "Enables the validation of a single space between parameter lists and open curly braces.",
    type = "BOOLEAN")
  public boolean isOneSpaceBetweenRParentAndLCurly = true;

  @RuleProperty(
    key = "one_space_before",
    defaultValue = "true",
    description = "Enables the validation of a single space between keyword and next tokens.",
    type = "BOOLEAN")
  public boolean isOneSpaceBetweenKeywordAndNextToken = true;

  @RuleProperty(
    key = "one_space_for",
    defaultValue = "true",
    description = "Enables the validation of a single space after loop semicolons.",
    type = "BOOLEAN")
  public boolean isOneSpaceAfterForLoopSemicolon = true;

  @RuleProperty(
    key = "space_comma",
    defaultValue = "true",
    description = "Enables the validation of a single space after commas.",
    type = "BOOLEAN")
  public boolean isOneSpaceAfterComma = true;

  @RuleProperty(
    key = "no_space_method_name",
    defaultValue = "true",
    description = "Enables the validation of no space after method names.",
    type = "BOOLEAN")
  public boolean isNoSpaceAfterMethodName = true;

  @RuleProperty(
    key = "foreach_space",
    defaultValue = "true",
    description = "Enables the validation of spaces in foreach statements.",
    type = "BOOLEAN")
  public boolean isSpaceForeachStatement = true;

  @RuleProperty(
    key = "no_space",
    defaultValue = "true",
    description = "Enables the validation of no spaces after open and before closing parenthesis.",
    type = "BOOLEAN")
  public boolean isNoSpaceParenthesis = true;

  @RuleProperty(
    key = "closure_format",
    defaultValue = "true",
    description = "Enables the validation of spaces in closure declarations.",
    type = "BOOLEAN")
  public boolean isClosureSpacing = true;

  /**
   * Indentation
   */
  @RuleProperty(
    key = "function_declaration_arguments_indentation",
    defaultValue = "true",
    description = "Enables the validation of parameter indentation in function declarations.",
    type = "BOOLEAN")
  public boolean isMethodArgumentsIndentation = true;

  @RuleProperty(
    key = "function_calls_arguments_indentation",
    defaultValue = "true",
    description = "Enables the validation of argument indentation in function calls.",
    type = "BOOLEAN")
  public boolean isFunctionCallsArgumentsIndentation = true;

  @RuleProperty(
    key = "interfaces_indentation",
    defaultValue = "true",
    description = "Enables the validation of interface indentation in call declarations.",
    type = "BOOLEAN")
  public boolean isInterfacesIndentation = true;

  /**
   * Extends and implements line
   */
  @RuleProperty(
    key = "extends_implements_line",
    defaultValue = "true",
    description = "Enables the verification that \"extends\" and \"implements\" are on the same line as class declarations.",
    type = "BOOLEAN")
  public boolean isExtendsAndImplementsLine = true;

  private static final Set<String> INTERNAL_FUNCTIONS = Set.of(
    PHPKeyword.ECHO.getValue(),
    PHPKeyword.ISSET.getValue(),
    PHPKeyword.EMPTY.getValue(),
    PHPKeyword.INCLUDE_ONCE.getValue(),
    PHPKeyword.INCLUDE.getValue(),
    PHPKeyword.EVAL.getValue(),
    PHPKeyword.REQUIRE.getValue(),
    PHPKeyword.REQUIRE_ONCE.getValue(),
    PHPKeyword.CLONE.getValue(),
    PHPKeyword.PRINT.getValue(),
    PHPKeyword.HALT_COMPILER.getValue(),
    PHPKeyword.DIE.getValue(),
    PHPKeyword.EXIT.getValue());

  public boolean isInternalFunction(ExpressionTree callee) {
    String calleeString = callee.toString().toLowerCase(Locale.ROOT);
    return INTERNAL_FUNCTIONS.contains(calleeString);
  }

  @Override
  public void visitScript(ScriptTree tree) {
    for (FormattingCheck subCheck : SUB_CHECKS) {
      subCheck.checkFormat(this, tree);
    }
  }

  public void reportIssue(String msg, Tree... issueLocations) {
    PreciseIssue issue = context().newIssue(this, issueLocations[0], msg);
    for (int i = 1; i < issueLocations.length; i++) {
      issue.secondary(issueLocations[i], null);
    }
  }

}
