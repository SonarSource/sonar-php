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
import com.sonar.sslr.api.Grammar;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.formattingStandardCheck.CurlyBraceCheck;
import org.sonar.php.checks.formattingStandardCheck.NamespaceAndUseStatementCheck;
import org.sonar.php.checks.formattingStandardCheck.SpacingCheck;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.grammar.GrammarRuleKey;

import javax.annotation.Nullable;

@Rule(
  key = "S1808",
  priority = Priority.MINOR)
public class FormattingStandardCheck extends SquidCheck<Grammar> {

  public static final GrammarRuleKey[] CLASS_AND_FUNCTION = {
    PHPGrammar.CLASS_DECLARATION,
    PHPGrammar.INTERFACE_DECLARATION,
    PHPGrammar.TRAIT_ADAPTATIONS,
    PHPGrammar.METHOD_DECLARATION,
    PHPGrammar.FUNCTION_DECLARATION
  };

  public static final GrammarRuleKey[] CONTROL_STRUCTURE = {
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
  private final SpacingCheck spacingCheck = new SpacingCheck();

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
   * Open curly brace
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

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.NAMESPACE_STATEMENT,
      PHPGrammar.USE_STATEMENT);
    subscribeTo(CLASS_AND_FUNCTION);
    subscribeTo(CONTROL_STRUCTURE);
    subscribeTo(PHPPunctuator.RPARENTHESIS);
  }

  @Override
  public void visitNode(AstNode astNode) {
    namespaceAndUseStatementCheck.visitNode(this, astNode);
    curlyBraceCheck.visitNode(this, astNode);
    spacingCheck.visitNode(this, astNode);
  }

  @Override
  public void leaveNode(AstNode astNode) {
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    namespaceAndUseStatementCheck.leaveFile();
  }

  public void reportIssue(String msg, AstNode node) {
    getContext().createLineViolation(this, msg, node);
  }

}
