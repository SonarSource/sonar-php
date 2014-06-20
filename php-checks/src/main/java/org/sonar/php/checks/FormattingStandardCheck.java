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
import org.sonar.api.PropertyType;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.formattingStandardCheck.NamespaceAndUseStatementCheck;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;

import javax.annotation.Nullable;

@Rule(
  key = "S1808",
  priority = Priority.MINOR)
public class FormattingStandardCheck extends SquidCheck<Grammar> {


  private NamespaceAndUseStatementCheck namespaceAndUseStatementCheck = new NamespaceAndUseStatementCheck();

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

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.NAMESPACE_STATEMENT,
      PHPGrammar.USE_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    namespaceAndUseStatementCheck.visitNode(this, astNode);
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
