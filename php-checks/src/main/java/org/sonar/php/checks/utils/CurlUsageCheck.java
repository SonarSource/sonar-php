/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks.utils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.sonar.php.tree.visitors.AssignmentExpressionVisitor;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public abstract class CurlUsageCheck extends PHPVisitorCheck {

  protected static final String CURL_SETOPT = "curl_setopt";

  private AssignmentExpressionVisitor assignmentExpressionVisitor;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    assignmentExpressionVisitor = new AssignmentExpressionVisitor(context().symbolTable());
    tree.accept(assignmentExpressionVisitor);
    super.visitCompilationUnit(tree);
  }

  protected abstract void addIssue(ExpressionTree expressionTree);

  protected void checkCurlSetop(List<ExpressionTree> arguments, String curloptSslVerifypeer, Set<String> verifyPeerCompliantValues) {
    ExpressionTree optionArgument = arguments.get(1);
    ExpressionTree valueArgument = arguments.get(2);

    nameOf(optionArgument).ifPresent(name -> {
      if (name.equals(curloptSslVerifypeer)) {
        this.checkCURLSSLVerify(valueArgument, verifyPeerCompliantValues);
      }
    });
  }

  protected static Optional<String> nameOf(Tree tree) {
    String name = CheckUtils.nameOf(tree);
    return name != null ? Optional.of(name) : Optional.empty();
  }

  protected void checkCURLSSLVerify(ExpressionTree expressionTree, Set<String> compliantValues) {
    ExpressionTree curlOptValue = getAssignedValue(expressionTree);
    if (curlOptValue instanceof LiteralTree) {
      String value = ((LiteralTree) curlOptValue).value();
      String quoteLessLowercaseValue = CheckUtils.trimQuotes(value).toLowerCase(Locale.ENGLISH);
      if (!compliantValues.contains(quoteLessLowercaseValue)) {
        addIssue(expressionTree);
      }
    }
  }

  protected ExpressionTree getAssignedValue(ExpressionTree value) {
    if (value.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      Symbol valueSymbol = context().symbolTable().getSymbol(value);
      return assignmentExpressionVisitor
        .getUniqueAssignedValue(valueSymbol)
        .orElse(value);
    }
    return value;
  }

}
